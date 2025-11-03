// lib/ui/pages/gabarito_page.dart
import 'package:flutter/material.dart';
import 'package:hackathonflutter/models/aluno.dart';
import 'package:hackathonflutter/models/questao.dart';
import 'package:hackathonflutter/models/resposta.dart';
import 'package:hackathonflutter/models/prova.dart';
import 'package:hackathonflutter/models/resposta_simples_dto.dart';
import 'package:hackathonflutter/services/aluno_service.dart';
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
    this.aluno,
    required this.prova,
    this.isViewingGabarito = false,
  });

  @override
  State<GabaritoPage> createState() => _GabaritoPageState();
}

class _GabaritoPageState extends State<GabaritoPage> {
  late AvaliacaoService _avaliacaoService;
  late AlunoService _alunoService;
  late OcrService _ocrService;
  List<Questao> _questoes = [];

  Map<String, TextEditingController> _controllers = {};
  List<RespostaSimplesDTO> _respostasSalvas = [];
  Aluno? _alunoSelecionado;
  Prova? _provaSelecionada;

  bool _carregando = true;
  bool _enviando = false;
  bool _canReadFromOcr = true;

  @override
  void initState() {
    super.initState();
    _avaliacaoService = Provider.of<AvaliacaoService>(context, listen: false);
    _alunoService = Provider.of<AlunoService>(context, listen: false);
    _ocrService = Provider.of<OcrService>(context, listen: false);
    _alunoSelecionado = widget.aluno;
    _provaSelecionada = widget.prova;
    _carregarQuestoes();
  }

  @override
  void dispose() {
    _controllers.forEach((key, controller) => controller.dispose());
    super.dispose();
  }

  // Carrega as questões E as respostas salvas do aluno
  Future<void> _carregarQuestoes() async {
    setState(() { _carregando = true; });

    if (_provaSelecionada == null) {
      if(mounted) MsgAlerta.showError(context, 'Erro', 'Nenhuma prova selecionada.');
      setState(() { _carregando = false; });
      return;
    }

    try {
      final questoesPromise = _avaliacaoService.buscarQuestoesProva(_provaSelecionada!.id);
      final respostasPromise = (widget.aluno != null && !widget.isViewingGabarito)
          ? _avaliacaoService.buscarRespostasDoAluno(widget.aluno!.id, _provaSelecionada!.id)
          : Future.value(<RespostaSimplesDTO>[]);

      final results = await Future.wait([questoesPromise, respostasPromise]);

      final List<Questao> questoesCarregadas = results[0] as List<Questao>;
      _respostasSalvas = results[1] as List<RespostaSimplesDTO>;

      if (questoesCarregadas.isEmpty) {
        if (mounted) {
          MsgAlerta.showWarning(
              context,
              'Atenção',
              'Nenhuma questão cadastrada para esta prova.');
        }
      }

      setState(() {
        _questoes = questoesCarregadas;
        _inicializarControllers();

        if (widget.isViewingGabarito && _provaSelecionada!.respostasCorretas != null) {
          _preencherRespostasCorretas();
        }
      });

    } catch (e) {
      if (mounted) {
        MsgAlerta.showError(
            context, 'Erro', 'Falha ao carregar questões: $e');
      }
      setState(() { _questoes = []; });
    } finally {
      setState(() { _carregando = false; });
    }
  }

  // Preenche os campos de texto com as respostas salvas
  void _inicializarControllers() {
    _controllers.clear();
    for (var questao in _questoes) {
      final respostaSalva = _respostasSalvas.firstWhere(
            (r) => r.numeroQuestao == questao.numero,
        orElse: () => RespostaSimplesDTO(numeroQuestao: '', alternativaEscolhida: ''),
      );
      _controllers[questao.numero] = TextEditingController(
          text: respostaSalva.alternativaEscolhida
      );
    }
  }

  // Preenche os campos com as respostas lidas do OCR
  void _preencherRespostasOCR(List<Map<String, String>> respostasLidas) {
    if (mounted) {
      setState(() {
        for (var respostaLida in respostasLidas) {
          final String? questaoNumeroStr = respostaLida['questao'];
          final String? alternativa = respostaLida['resposta']?.toUpperCase();
          if (questaoNumeroStr != null && alternativa != null && _controllers.containsKey(questaoNumeroStr)) {
            _controllers[questaoNumeroStr]!.text = alternativa;
          }
        }
        _canReadFromOcr = false;
      });
      MsgAlerta.showSuccess(context, 'Sucesso', 'Respostas do gabarito pré-preenchidas!');
    }
  }

  // Preenche os campos com o gabarito correto da prova
  void _preencherRespostasCorretas() {
    if (_provaSelecionada?.respostasCorretas != null) {
      for (var respostaCorreta in _provaSelecionada!.respostasCorretas) {
        if (_controllers.containsKey(respostaCorreta.questaoNumero)) {
          _controllers[respostaCorreta.questaoNumero]!.text = respostaCorreta.alternativaSelecionada;
        }
      }
    }
  }

  // --- MÉTODO _enviarRespostas ATUALIZADO (com a correção da navegação) ---
  Future<void> _enviarRespostas() async {
    if (!widget.isViewingGabarito && _alunoSelecionado == null) {
      MsgAlerta.showWarning(context, 'Atenção', 'Nenhum aluno selecionado.');
      return;
    }
    if (_questoes.isEmpty) {
      MsgAlerta.showWarning(context, 'Atenção', 'Não há questões para enviar.');
      return;
    }

    setState(() { _enviando = true; });

    List<Map<String, String>> respostasAluno = [];
    int respostasPreenchidas = 0;

    for (var questao in _questoes) {
      final controller = _controllers[questao.numero];
      if (controller != null && controller.text.isNotEmpty) {
        respostasAluno.add({
          'questao': questao.numero, // String
          'resposta': controller.text.toUpperCase(), // String
        });
        respostasPreenchidas++;
      }
    }

    if (respostasPreenchidas < _questoes.length) {
      final bool confirmar = await MsgAlerta.showConfirm(
          context,
          'Questões em Branco',
          'Você respondeu apenas $respostasPreenchidas de ${_questoes.length} questões. Deseja enviar mesmo assim?');
      if (!confirmar) {
        setState(() { _enviando = false; });
        return;
      }
    }

    try {
      final response = await _avaliacaoService.enviarGabaritoAluno(
        _alunoSelecionado!.id,
        _provaSelecionada!.id,
        respostasAluno,
      );

      if (response['status'] == 'success') {

        // --- INÍCIO DA CORREÇÃO ---
        if (!mounted) return;
        final String mensagemSucesso = response['message'] ?? 'Gabarito enviado com sucesso!';

        // 1. Mostre o alerta de sucesso PRIMEIRO.
        // O await garante que esperamos o utilizador clicar "OK".
        await MsgAlerta.showSuccess(context, 'Sucesso', mensagemSucesso);

        // 2. AGORA que o alerta foi fechado, o 'context' ainda é válido.
        // Verifique novamente (por segurança) e feche a GabaritoPage.
        if (!mounted) return;
        Navigator.pop(context); // Volta para a ListagemPage
        // --- FIM DA CORREÇÃO ---

      } else {
        if (mounted) {
          MsgAlerta.showError(context, 'Erro',
              response['message'] ?? 'Falha ao enviar gabarito.');
        }
      }
    } catch (e) {
      if (mounted) {
        MsgAlerta.showError(
            context, 'Erro de Envio', 'Não foi possível enviar o gabarito: $e');
      }
    } finally {
      if (mounted) {
        setState(() {
          _enviando = false;
        });
      }
    }
  }

  // MÉTODO _lerGabaritoCamera ATUALIZADO
  Future<void> _lerGabaritoCamera() async {
    // 1. Navega para a CameraScreen e espera o resultado
    final Map<String, dynamic>? result = await Navigator.push(
      context,
      MaterialPageRoute(builder: (context) => CameraScreen()), // 'const' removido
    );

    if (result == null || result.isEmpty || !mounted) return;

    // 2. Extrai os IDs (Strings) e Respostas do mapa retornado pelo OcrService
    final String? alunoIdStr = result['alunoId'];
    final String? provaIdStr = result['provaId'];
    final List<Map<String, String>> respostasOCR = List<Map<String, String>>.from(result['respostas'] ?? []);

    setState(() { _carregando = true; });

    try {
      Aluno? alunoOCR;
      Prova? provaOCR;

      // 4. Busca o Aluno completo usando o ID do OCR
      if (alunoIdStr != null && alunoIdStr.isNotEmpty) {
        final int? alunoId = int.tryParse(alunoIdStr);
        if (alunoId != null) {
          alunoOCR = await _alunoService.buscarAlunoPorId(alunoId);
        }
      }

      // 5. Busca a Prova completa usando o ID do OCR
      if (provaIdStr != null && provaIdStr.isNotEmpty) {
        final int? provaId = int.tryParse(provaIdStr);
        if (provaId != null) {
          provaOCR = await _avaliacaoService.buscarProvaPorId(provaId);
        }
      }

      bool provaMudou = false;

      // 6. Atualiza o estado da página com os dados encontrados
      setState(() {
        if (alunoOCR != null) {
          _alunoSelecionado = alunoOCR;
        }
        if (provaOCR != null && provaOCR.id != _provaSelecionada?.id) {
          _provaSelecionada = provaOCR;
          provaMudou = true;
        }
      });

      // 7. Se a prova mudou, recarrega as questões
      if (provaMudou) {
        await _carregarQuestoes();
      }

      // 8. Preenche as respostas lidas
      if (respostasOCR.isNotEmpty) {
        _preencherRespostasOCR(respostasOCR);
      }

      if (alunoOCR != null && provaOCR != null) {
        MsgAlerta.showSuccess(
            context, 'OCR Concluído', 'Aluno e Prova identificados e respostas preenchidas!');
      } else {
        MsgAlerta.showWarning(context, 'OCR Parcial', 'Respostas preenchidas, mas Aluno ou Prova não foram identificados.');
      }

    } catch (e) {
      if (mounted) MsgAlerta.showError(context, 'Erro no OCR', 'Falha ao processar dados do OCR: $e');
    } finally {
      if (mounted) setState(() { _carregando = false; });
    }
  }

  // Lida com o botão "voltar"
  Future<bool> _onBackPressed() async {
    if (_enviando) {
      return false;
    }
    bool temAlteracoes = false;
    for (var questao in _questoes) {
      final controller = _controllers[questao.numero];
      final respostaSalva = _respostasSalvas.firstWhere(
            (r) => r.numeroQuestao == questao.numero,
        orElse: () => RespostaSimplesDTO(numeroQuestao: '', alternativaEscolhida: ''),
      );

      if (controller != null && controller.text.toUpperCase() != respostaSalva.alternativaEscolhida.toUpperCase()) {
        temAlteracoes = true;
        break;
      }
    }

    if (temAlteracoes && !widget.isViewingGabarito) {
      final bool confirmar = await MsgAlerta.showConfirm(
          context,
          'Alterações não Salvas',
          'Você tem respostas alteradas que não foram enviadas. Deseja sair mesmo assim?');
      return confirmar;
    }
    return true;
  }

  String get _appBarTitle {
    if (widget.isViewingGabarito) {
      return 'Gabarito Correto: ${widget.prova.disciplinaNome}';
    } else {
      if (_alunoSelecionado != null) {
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
                  'Prova: ${_provaSelecionada?.titulo ?? widget.prova.titulo}',
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
            if (!widget.isViewingGabarito &&
                _alunoSelecionado != null)
              Container(
                color:
                Theme.of(context).primaryColor.withOpacity(0.1),
                padding: const EdgeInsets.all(12),
                child: Row(
                  children: [
                    const Icon(Icons.person, size: 20),
                    const SizedBox(width: 8),
                    Expanded(
                      child: Text(
                        'Aluno: ${_alunoSelecionado!.nome}',
                        style: const TextStyle(
                            fontWeight: FontWeight.bold),
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
                  if (controller == null)
                    return const SizedBox.shrink();

                  return Card(
                    margin:
                    const EdgeInsets.symmetric(vertical: 8.0),
                    child: Padding(
                      padding: const EdgeInsets.all(16.0),
                      child: Column(
                        crossAxisAlignment:
                        CrossAxisAlignment.start,
                        children: [
                          Text(
                            'Questão ${questao.numero}:',
                            style: Theme.of(context)
                                .textTheme
                                .titleMedium,
                          ),
                          const SizedBox(height: 8.0),
                          Row(
                            children: [
                              const Text(
                                'Resposta: ',
                                style: TextStyle(
                                    fontWeight: FontWeight.bold),
                              ),
                              Expanded(
                                child: TextField(
                                  controller: controller,
                                  enabled: !widget.isViewingGabarito,
                                  maxLength: 1,
                                  textCapitalization:
                                  TextCapitalization.characters,
                                  decoration:
                                  const InputDecoration(
                                    border: OutlineInputBorder(),
                                    contentPadding:
                                    EdgeInsets.symmetric(
                                      horizontal: 10,
                                      vertical: 8,
                                    ),
                                    counterText: '',
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
                      label: Text(_respostasSalvas.isNotEmpty
                          ? 'Atualizar Respostas'
                          : 'Enviar Respostas'),
                      style: ElevatedButton.styleFrom(
                        minimumSize:
                        const Size(double.infinity, 50),
                      ),
                    ),
                  ],
                ),
              ),
          ],
        ),

        floatingActionButton: !widget.isViewingGabarito && // Corrigido
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