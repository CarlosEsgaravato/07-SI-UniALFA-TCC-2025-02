// lib/ui/pages/gabarito_page.dart
import 'package:flutter/material.dart';
import 'package:hackathonflutter/models/aluno.dart';
import 'package:hackathonflutter/models/questao.dart';
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

  // --- CORREÇÃO AQUI ---
  // A chave do Map agora é 'int' (número da questão)
  // em vez de 'String'.
  Map<int, TextEditingController> _controllers = {};
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

        // (Lógica de preencher gabarito correto não precisa de mudança)
        // if (widget.isViewingGabarito && _provaSelecionada!.respostasCorretas != null) {
        //   _preencherRespostasCorretas();
        // }
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
  // --- CORREÇÃO AQUI ---
  void _inicializarControllers() {
    _controllers.clear();
    for (var questao in _questoes) {
      // Converte o número da questão (ex: "01") para um int (ex: 1)
      final int? questaoNumInt = int.tryParse(questao.numero);
      if (questaoNumInt == null) continue; // Ignora se a chave for inválida (ex: "A1")

      // Encontra a resposta salva comparando os ints
      final respostaSalva = _respostasSalvas.firstWhere(
            (r) => (int.tryParse(r.numeroQuestao) ?? -1) == questaoNumInt,
        orElse: () => RespostaSimplesDTO(numeroQuestao: '', alternativaEscolhida: ''),
      );

      // Salva no Map usando a CHAVE INT
      _controllers[questaoNumInt] = TextEditingController(
          text: respostaSalva.alternativaEscolhida
      );
    }
  }

  // Preenche os campos com as respostas lidas do OCR
  // --- CORREÇÃO AQUI ---
  void _preencherRespostasOCR(List<Map<String, String>> respostasLidas) {
    if (mounted) {
      setState(() {
        for (var respostaLida in respostasLidas) {
          final String? questaoNumeroStr = respostaLida['questao'];
          final String? alternativa = respostaLida['resposta']?.toUpperCase();

          // Converte o número lido (ex: "1") para um int (ex: 1)
          final int? questaoNumInt = int.tryParse(questaoNumeroStr ?? '');

          // Verifica se o controller existe usando a CHAVE INT
          if (questaoNumInt != null && alternativa != null && _controllers.containsKey(questaoNumInt)) {
            // Atualiza o controller usando a CHAVE INT
            _controllers[questaoNumInt]!.text = alternativa;
          }
        }
        _canReadFromOcr = false;
      });
    }
  }

  // (Método _preencherRespostasCorretas removido para simplificar,
  //  pois _inicializarControllers agora lida com o preenchimento inicial)

  // --- CORREÇÃO AQUI ---
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
      // Converte o número da questão (ex: "01") para int (ex: 1)
      final int? questaoNumInt = int.tryParse(questao.numero);
      if (questaoNumInt == null) continue;

      // Pega o controller usando a CHAVE INT
      final controller = _controllers[questaoNumInt];

      if (controller != null && controller.text.isNotEmpty) {
        respostasAluno.add({
          'questao': questao.numero, // Envia a string ORIGINAL ("01") para a API
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

        if (!mounted) return;
        final String mensagemSucesso = response['message'] ?? 'Gabarito enviado com sucesso!';

        await MsgAlerta.showSuccess(context, 'Sucesso', mensagemSucesso);

        if (!mounted) return;
        Navigator.pop(context); // Volta para a ListagemPage

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

  // MÉTODO _lerGabaritoCamera (Já estava correto da última vez)
  Future<void> _lerGabaritoCamera() async {
    final Map<String, dynamic>? result = await Navigator.push(
      context,
      MaterialPageRoute(builder: (context) => const CameraScreen()),
    );

    if (result == null || !mounted) return;

    final List<Map<String, String>> respostasOCR =
    List<Map<String, String>>.from(result['respostas'] ?? []);

    if (respostasOCR.isEmpty) {
      MsgAlerta.showWarning(context, 'OCR',
          'Nenhuma resposta foi lida. O preenchimento continua manual.');
      return;
    }

    try {
      // ESTA FUNÇÃO AGORA ESTÁ CORRIGIDA
      _preencherRespostasOCR(respostasOCR);

      MsgAlerta.showSuccess(context, 'OCR Concluído',
          'Respostas lidas preenchidas! Verifique e envie.');

    } catch (e) {
      if (mounted) {
        MsgAlerta.showError(
            context, 'Erro no OCR', 'Falha ao processar dados do OCR: $e');
      }
    }
  }


  // Lida com o botão "voltar"
  // --- CORREÇÃO AQUI ---
  Future<bool> _onBackPressed() async {
    if (_enviando) {
      return false;
    }
    bool temAlteracoes = false;
    for (var questao in _questoes) {
      // Converte o número da questão (ex: "01") para int (ex: 1)
      final int? questaoNumInt = int.tryParse(questao.numero);
      if (questaoNumInt == null) continue;

      // Pega o controller usando a CHAVE INT
      final controller = _controllers[questaoNumInt];

      // Encontra a resposta salva comparando os ints
      final respostaSalva = _respostasSalvas.firstWhere(
            (r) => (int.tryParse(r.numeroQuestao) ?? -1) == questaoNumInt,
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

  // (Método _appBarTitle não precisa de alteração)
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
            ? Center( // (Layout de 'Nenhuma questão' não precisa de alteração)
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
            // (Layout do 'Nome do Aluno' não precisa de alteração)
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
              // --- CORREÇÃO AQUI ---
              child: ListView.builder(
                padding: const EdgeInsets.all(16.0),
                itemCount: _questoes.length,
                itemBuilder: (context, index) {
                  final questao = _questoes[index];
                  // Converte o número da questão (ex: "01") para int (ex: 1)
                  final int? questaoNumInt = int.tryParse(questao.numero);

                  // Pega o controller usando a CHAVE INT
                  final controller = (questaoNumInt != null)
                      ? _controllers[questaoNumInt]
                      : null;

                  if (controller == null)
                    return const SizedBox.shrink(); // Ignora se a chave foi inválida

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
                            'Questão ${questao.numero}:', // Mostra a string original "01"
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
                                  controller: controller, // Usa o controller correto
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

            // (Layout do 'Botão Enviar' não precisa de alteração)
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
        // (Layout do 'Botão Flutuante' não precisa de alteração)
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