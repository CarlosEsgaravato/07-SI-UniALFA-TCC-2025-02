// lib/ui/pages/gabarito_page.dart
import 'package:flutter/material.dart';
import 'package:hackathonflutter/models/aluno.dart';
import 'package:hackathonflutter/models/questao.dart';
import 'package:hackathonflutter/models/resposta.dart';
import 'package:hackathonflutter/models/prova.dart';
import 'package:hackathonflutter/services/avaliacao_service.dart';
import 'package:hackathonflutter/services/ocr_service.dart';
import 'package:hackathonflutter/ui/widgets/circulo_espera.dart';
import 'package:hackathonflutter/ui/widgets/msg_alerta.dart';
import 'package:hackathonflutter/ui/widgets/botao_flutuante.dart';
import 'package:hackathonflutter/screens/camera_screen.dart';
import 'package:provider/provider.dart';

class GabaritoPage extends StatefulWidget {
  final Aluno? aluno;
  final Prova prova;
  final bool isViewingGabarito;

  const GabaritoPage({
    super.key,
    this.aluno, // Aluno é opcional agora para gabarito correto
    required this.prova,
    this.isViewingGabarito = false,
  });

  @override
  State<GabaritoPage> createState() => _GabaritoPageState();
}

class _GabaritoPageState extends State<GabaritoPage> {
  late AvaliacaoService _avaliacaoService;
  late OcrService _ocrService;
  List<Questao> _questoes = [];
  Map<int, TextEditingController> _controllers = {};
  Aluno? _alunoSelecionado;
  Prova? _provaSelecionada;

  bool _carregando = true;
  bool _enviando = false;
  bool _canReadFromOcr = true;

  @override
  void initState() {
    super.initState();
    _avaliacaoService = Provider.of<AvaliacaoService>(context, listen: false);
    _ocrService = Provider.of<OcrService>(context, listen: false);

    // Inicializar com os valores do widget
    _alunoSelecionado = widget.aluno;
    _provaSelecionada = widget.prova; // IMPORTANTE: usar widget.prova, não null

    // Carregar questões imediatamente
    _carregarQuestoes();
  }

  @override
  void dispose() {
    _controllers.forEach((key, controller) => controller.dispose());
    super.dispose();
  }

  Future<void> _carregarQuestoes() async {
    setState(() {
      _carregando = true;
    });

    try {
      // Usar widget.prova.id diretamente já que é required
      final List<Questao> questoesCarregadas = await _avaliacaoService.buscarQuestoesProva(widget.prova.id);

      if (questoesCarregadas.isEmpty) {
        if (mounted) {
          MsgAlerta.showWarning(
              context,
              'Atenção',
              'Nenhuma questão cadastrada para esta prova. Verifique se as questões foram cadastradas no sistema.'
          );
        }
      }

      setState(() {
        _questoes = questoesCarregadas;
        _inicializarControllers();

        // Se for para visualizar gabarito correto, preencher automaticamente
        if (widget.isViewingGabarito && widget.prova.respostasCorretas != null) {
          _preencherRespostasCorretas();
        }
      });

      if (mounted && questoesCarregadas.isNotEmpty) {
        print('Carregadas ${questoesCarregadas.length} questões para a prova ${widget.prova.id}');
      }
    } catch (e) {
      if (mounted) {
        MsgAlerta.showError(
            context,
            'Erro',
            'Falha ao carregar questões: $e'
        );
      }
      setState(() {
        _questoes = [];
      });
    } finally {
      setState(() {
        _carregando = false;
      });
    }
  }

  void _inicializarControllers() {
    _controllers.clear();
    for (var questao in _questoes) {
      _controllers[questao.numero] = TextEditingController();
    }
  }

  void _preencherRespostasOCR(List<Map<String, String>> respostasLidas) {
    if (mounted) {
      setState(() {
        for (var respostaLida in respostasLidas) {
          final int? questaoNumero = int.tryParse(respostaLida['questao'] ?? '');
          final String? alternativa = respostaLida['resposta']?.toUpperCase();

          if (questaoNumero != null && alternativa != null && _controllers.containsKey(questaoNumero)) {
            _controllers[questaoNumero]!.text = alternativa;
          }
        }
        _canReadFromOcr = false;
      });
      MsgAlerta.showSuccess(context, 'Sucesso', 'Respostas do gabarito pré-preenchidas!');
    }
  }

  void _preencherRespostasCorretas() {
    if (widget.prova.respostasCorretas != null) {
      for (var respostaCorreta in widget.prova.respostasCorretas!) {
        if (_controllers.containsKey(respostaCorreta.questaoNumero)) {
          _controllers[respostaCorreta.questaoNumero]!.text = respostaCorreta.alternativaSelecionada;
        }
      }
    }
  }

  Future<void> _enviarRespostas() async {
    // Verificar se temos aluno selecionado (não necessário se for visualização de gabarito)
    if (!widget.isViewingGabarito && _alunoSelecionado == null) {
      MsgAlerta.showWarning(
          context,
          'Atenção',
          'Nenhum aluno selecionado. Por favor, volte e selecione um aluno.'
      );
      return;
    }

    // Verificar se há questões
    if (_questoes.isEmpty) {
      MsgAlerta.showWarning(
          context,
          'Atenção',
          'Não há questões para enviar respostas.'
      );
      return;
    }

    setState(() {
      _enviando = true;
    });

    List<Map<String, String>> respostasAluno = [];
    int respostasPreenchidas = 0;

    for (var questao in _questoes) {
      final controller = _controllers[questao.numero];
      if (controller != null && controller.text.isNotEmpty) {
        respostasAluno.add({
          'questao': questao.numero.toString(),
          'resposta': controller.text.toUpperCase(),
        });
        respostasPreenchidas++;
      }
    }

    // Avisar se nem todas as questões foram respondidas
    if (respostasPreenchidas < _questoes.length) {
      final bool confirmar = await MsgAlerta.showConfirm(
          context,
          'Questões em Branco',
          'Você respondeu apenas $respostasPreenchidas de ${_questoes.length} questões. Deseja enviar mesmo assim?'
      );
      if (!confirmar) {
        setState(() {
          _enviando = false;
        });
        return;
      }
    }

    try {
      final response = await _avaliacaoService.enviarGabaritoAluno(
        _alunoSelecionado!.id,
        widget.prova.id,
        respostasAluno,
      );

      if (response['status'] == 'success') {
        if (mounted) {
          MsgAlerta.showSuccess(
              context,
              'Sucesso',
              response['message'] ?? 'Gabarito enviado com sucesso!'
          );
          Navigator.pop(context);
        }
      } else {
        if (mounted) {
          MsgAlerta.showError(
              context,
              'Erro',
              response['message'] ?? 'Falha ao enviar gabarito.'
          );
        }
      }
    } catch (e) {
      if (mounted) {
        MsgAlerta.showError(
            context,
            'Erro de Envio',
            'Não foi possível enviar o gabarito: $e'
        );
      }
    } finally {
      setState(() {
        _enviando = false;
      });
    }
  }

  Future<void> _lerGabaritoCamera() async {
    final Map<String, dynamic>? result = await Navigator.push(
      context,
      MaterialPageRoute(builder: (context) => const CameraScreen()),
    );

    if (result != null && result.isNotEmpty) {
      final Aluno? alunoOCR = result['aluno'];
      final Prova? provaOCR = result['prova'];
      final List<Map<String, String>> respostasOCR = List<Map<String, String>>.from(result['respostas'] ?? []);

      // Atualizar os dados se foram lidos do OCR
      setState(() {
        if (alunoOCR != null) {
          _alunoSelecionado = alunoOCR;
        }
        if (provaOCR != null) {
          _provaSelecionada = provaOCR;
          // Se mudou a prova, recarregar as questões
          if (provaOCR.id != widget.prova.id) {
            _carregarQuestoes();
          }
        }
      });

      // Preencher as respostas
      if (respostasOCR.isNotEmpty) {
        _preencherRespostasOCR(respostasOCR);
      }

      if (_alunoSelecionado != null && _provaSelecionada != null) {
        MsgAlerta.showSuccess(
            context,
            'OCR Concluído',
            'Dados identificados e respostas preenchidas!'
        );
      }
    }
  }

  Future<bool> _onBackPressed() async {
    if (_enviando) {
      return false;
    }

    // Verificar se há alterações não salvas
    bool temAlteracoes = false;
    for (var controller in _controllers.values) {
      if (controller.text.isNotEmpty) {
        temAlteracoes = true;
        break;
      }
    }

    if (temAlteracoes && !widget.isViewingGabarito) {
      final bool confirmar = await MsgAlerta.showConfirm(
          context,
          'Alterações não Salvas',
          'Você tem respostas preenchidas que não foram enviadas. Deseja sair mesmo assim?'
      );
      return confirmar;
    }

    return true;
  }

  String get _appBarTitle {
    if (widget.isViewingGabarito) {
      return 'Gabarito Correto: ${widget.prova.disciplinaNome}';
    } else {
      if (_alunoSelecionado != null && _provaSelecionada != null) {
        return 'Gabarito: ${_alunoSelecionado!.nome}';
      } else if (_alunoSelecionado != null) {
        return 'Gabarito: ${_alunoSelecionado!.nome}';
      } else if (_provaSelecionada != null) {
        return 'Gabarito: ${_provaSelecionada!.disciplinaNome}';
      }
      return 'Lançar Gabarito';
    }
  }

  @override
  Widget build(BuildContext context) {
    return PopScope(
      canPop: true,
      onPopInvoked: (didPop) async {
        if (didPop) return;
        final shouldPop = await _onBackPressed();
        if (shouldPop && mounted) {
          Navigator.of(context).pop();
        }
      },
      child: Scaffold(
        appBar: AppBar(
          title: Text(_appBarTitle),
        ),
        body: _carregando
            ? const CirculoEspera()
            : _questoes.isEmpty
            ? Center(
          child: Padding(
            padding: const EdgeInsets.all(16.0),
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                const Icon(
                  Icons.quiz_outlined,
                  size: 80,
                  color: Colors.grey,
                ),
                const SizedBox(height: 16),
                const Text(
                  'Nenhuma questão encontrada para esta prova.',
                  style: TextStyle(fontSize: 18),
                  textAlign: TextAlign.center,
                ),
                const SizedBox(height: 8),
                Text(
                  'Prova: ${widget.prova.disciplinaNome}',
                  style: const TextStyle(
                    fontSize: 16,
                    color: Colors.grey,
                  ),
                ),
                const SizedBox(height: 24),
                ElevatedButton.icon(
                  onPressed: _carregarQuestoes,
                  icon: const Icon(Icons.refresh),
                  label: const Text('Tentar Novamente'),
                ),
              ],
            ),
          ),
        )
            : Column(
          children: [
            // Informações do gabarito
            if (!widget.isViewingGabarito && _alunoSelecionado != null)
              Container(
                color: Theme.of(context).primaryColor.withOpacity(0.1),
                padding: const EdgeInsets.all(12),
                child: Row(
                  children: [
                    const Icon(Icons.person, size: 20),
                    const SizedBox(width: 8),
                    Expanded(
                      child: Text(
                        'Aluno: ${_alunoSelecionado!.nome}',
                        style: const TextStyle(fontWeight: FontWeight.bold),
                      ),
                    ),
                  ],
                ),
              ),

            Expanded(
              child: ListView.builder(
                padding: const EdgeInsets.all(16.0),
                itemCount: _questoes.length,
                itemBuilder: (context, index) {
                  final questao = _questoes[index];
                  final controller = _controllers[questao.numero];
                  if (controller == null) return const SizedBox.shrink();

                  return Card(
                    margin: const EdgeInsets.symmetric(vertical: 8.0),
                    child: Padding(
                      padding: const EdgeInsets.all(16.0),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            'Questão ${questao.numero}:',
                            style: Theme.of(context).textTheme.titleMedium,
                          ),
                          const SizedBox(height: 8.0),
                          Row(
                            children: [
                              const Text(
                                'Resposta: ',
                                style: TextStyle(fontWeight: FontWeight.bold),
                              ),
                              Expanded(
                                child: TextField(
                                  controller: controller,
                                  enabled: !widget.isViewingGabarito,
                                  maxLength: 1,
                                  textCapitalization: TextCapitalization.characters,
                                  decoration: const InputDecoration(
                                    border: OutlineInputBorder(),
                                    contentPadding: EdgeInsets.symmetric(
                                      horizontal: 10,
                                      vertical: 8,
                                    ),
                                    counterText: '', // Remove o contador
                                  ),
                                  textAlign: TextAlign.center,
                                  style: const TextStyle(
                                    fontSize: 18,
                                    fontWeight: FontWeight.bold,
                                  ),
                                ),
                              ),
                            ],
                          ),
                        ],
                      ),
                    ),
                  );
                },
              ),
            ),

            // Botões na parte inferior
            if (!widget.isViewingGabarito && _questoes.isNotEmpty)
              Container(
                padding: const EdgeInsets.all(16.0),
                decoration: BoxDecoration(
                  color: Theme.of(context).scaffoldBackgroundColor,
                  boxShadow: [
                    BoxShadow(
                      color: Colors.black.withOpacity(0.1),
                      blurRadius: 4,
                      offset: const Offset(0, -2),
                    ),
                  ],
                ),
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    _enviando
                        ? const CirculoEspera()
                        : ElevatedButton.icon(
                      onPressed: _enviarRespostas,
                      icon: const Icon(Icons.send),
                      label: const Text('Enviar Respostas'),
                      style: ElevatedButton.styleFrom(
                        minimumSize: const Size(double.infinity, 50),
                      ),
                    ),
                  ],
                ),
              ),
          ],
        ),
        floatingActionButton: !widget.isViewingGabarito &&
            _questoes.isNotEmpty &&
            !_enviando
            ? BotaoFlutuante(
          icone: Icons.camera_alt,
          evento: _canReadFromOcr ? _lerGabaritoCamera : null,
        )
            : null,
      ),
    );
  }
}