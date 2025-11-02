// lib/screens/result_screen.dart
import 'package:flutter/material.dart';
import 'package:hackathonflutter/services/aluno_service.dart';
import 'package:hackathonflutter/services/avaliacao_service.dart';
import 'package:hackathonflutter/models/aluno.dart';
import 'package:hackathonflutter/models/prova.dart';
import 'package:hackathonflutter/ui/widgets/msg_alerta.dart';
import 'package:hackathonflutter/ui/widgets/circulo_espera.dart';
import 'package:provider/provider.dart';

class ResultScreen extends StatefulWidget {
  final Map<String, dynamic> extractedData;

  const ResultScreen({super.key, required this.extractedData});

  @override
  State<ResultScreen> createState() => _ResultScreenState();
}

class _ResultScreenState extends State<ResultScreen> {
  late TextEditingController _alunoIdController;
  late TextEditingController _provaIdController;
  late List<Map<String, String>> _respostas;
  late AlunoService _alunoService;
  late AvaliacaoService _avaliacaoService;

  Aluno? _alunoEncontrado; // Armazenar o objeto Aluno
  Prova? _provaEncontrada; // Armazenar o objeto Prova

  String _alunoNome = 'Buscando...';
  String _provaNome = 'Buscando...';
  bool _isLoading = false;
  bool _isSaving = false; // Para controlar o estado de salvamento

  @override
  void initState() {
    super.initState();
    _alunoService = Provider.of<AlunoService>(context, listen: false);
    _avaliacaoService = Provider.of<AvaliacaoService>(context, listen: false);

    _alunoIdController = TextEditingController(text: widget.extractedData['alunoId']?.toString() ?? '');
    _provaIdController = TextEditingController(text: widget.extractedData['provaId']?.toString() ?? '');
    _respostas = List<Map<String, String>>.from(widget.extractedData['respostas'] ?? []);

    _loadAlunoAndProvaDetails();
  }

  @override
  void dispose() {
    _alunoIdController.dispose();
    _provaIdController.dispose();
    super.dispose();
  }

  Future<void> _loadAlunoAndProvaDetails() async {
    setState(() {
      _isLoading = true;
      _alunoNome = 'Buscando...';
      _provaNome = 'Buscando...';
    });

    try {
      final int? alunoId = int.tryParse(_alunoIdController.text);
      final int? provaId = int.tryParse(_provaIdController.text);

      if (alunoId != null) {
        _alunoEncontrado = await _alunoService.buscarAlunoPorId(alunoId);
        if (_alunoEncontrado != null) {
          _alunoNome = _alunoEncontrado!.nome;
        } else {
          _alunoNome = 'Aluno não encontrado';
          if (mounted) {
            MsgAlerta.showError(context, 'Erro', 'Aluno com ID $alunoId não encontrado.');
          }
        }
      } else {
        _alunoNome = 'ID do Aluno inválido';
        if (mounted) {
          MsgAlerta.showWarning(context, 'Aviso', 'ID do Aluno não reconhecido ou vazio.');
        }
      }

      if (provaId != null) {
        _provaEncontrada = await _avaliacaoService.buscarProvaPorId(provaId);
        if (_provaEncontrada != null) {
          _provaNome = _provaEncontrada!.disciplinaNome;
        } else {
          _provaNome = 'Prova não encontrada';
          if (mounted) {
            MsgAlerta.showError(context, 'Erro', 'Prova com ID $provaId não encontrada.');
          }
        }
      } else {
        _provaNome = 'ID da Prova inválido';
        if (mounted) {
          MsgAlerta.showWarning(context, 'Aviso', 'ID da Prova não reconhecido ou vazio.');
        }
      }
    } catch (e) {
      _alunoNome = 'Erro ao buscar Aluno';
      _provaNome = 'Erro ao buscar Prova';
      if (mounted) {
        MsgAlerta.showError(context, 'Erro de Busca', 'Falha ao buscar detalhes: $e');
      }
    } finally {
      setState(() {
        _isLoading = false;
      });
    }
  }

  // Método para salvar diretamente (similar ao _enviarRespostas da GabaritoPage)
  Future<void> _salvarGabarito() async {
    // Validar se aluno e prova foram encontrados
    if (_alunoEncontrado == null) {
      MsgAlerta.showWarning(context, 'Atenção', 'Selecione ou corrija o ID do Aluno antes de salvar.');
      return;
    }
    if (_provaEncontrada == null) {
      MsgAlerta.showWarning(context, 'Atenção', 'Selecione ou corrija o ID da Prova antes de salvar.');
      return;
    }

    // Validar se há respostas
    if (_respostas.isEmpty) {
      MsgAlerta.showWarning(context, 'Atenção', 'Nenhuma resposta foi lida. Por favor, verifique o gabarito.');
      return;
    }

    setState(() {
      _isSaving = true;
    });

    try {
      final response = await _avaliacaoService.enviarGabaritoAluno(
        _alunoEncontrado!.id,
        _provaEncontrada!.id,
        _respostas,
      );

      if (response['status'] == 'success') {
        if (mounted) {
          MsgAlerta.showSuccess(
              context,
              'Sucesso',
              response['message'] ?? 'Gabarito salvo com sucesso!'
          );
          // Aguardar um pouco para o usuário ver a mensagem
          await Future.delayed(const Duration(seconds: 1));
          if (mounted) {
            Navigator.pop(context); // Volta para a tela anterior
          }
        }
      } else {
        if (mounted) {
          MsgAlerta.showError(
              context,
              'Erro',
              response['message'] ?? 'Falha ao salvar gabarito.'
          );
        }
      }
    } catch (e) {
      if (mounted) {
        MsgAlerta.showError(
            context,
            'Erro de Envio',
            'Não foi possível salvar o gabarito: $e'
        );
      }
    } finally {
      if (mounted) {
        setState(() {
          _isSaving = false;
        });
      }
    }
  }

  // Método para confirmar e devolver os dados para preenchimento manual
  void _confirmarEDevolver() {
    // Validar se aluno e prova foram encontrados
    if (_alunoEncontrado == null) {
      MsgAlerta.showWarning(context, 'Atenção', 'Selecione ou corrija o ID do Aluno antes de confirmar.');
      return;
    }
    if (_provaEncontrada == null) {
      MsgAlerta.showWarning(context, 'Atenção', 'Selecione ou corrija o ID da Prova antes de confirmar.');
      return;
    }

    Navigator.pop(context, {
      'aluno': _alunoEncontrado,
      'prova': _provaEncontrada,
      'respostas': _respostas,
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Confirmar Gabarito Lido'),
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Card(
              margin: const EdgeInsets.only(bottom: 16),
              child: Padding(
                padding: const EdgeInsets.all(16.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text('Dados Lidos pelo OCR:', style: Theme.of(context).textTheme.titleMedium),
                    const SizedBox(height: 10),
                    Row(
                      children: [
                        const Text('Aluno ID: '),
                        Expanded(
                          child: TextField(
                            controller: _alunoIdController,
                            keyboardType: TextInputType.number,
                            onChanged: (_) => _loadAlunoAndProvaDetails(), // Recarregar ao mudar ID
                            decoration: const InputDecoration(
                              hintText: 'Digite o ID do Aluno',
                              border: OutlineInputBorder(),
                              isDense: true,
                            ),
                          ),
                        ),
                        const SizedBox(width: 8),
                        Expanded(
                          flex: 2,
                          child: Text(
                            _alunoNome,
                            style: TextStyle(
                              fontWeight: FontWeight.bold,
                              color: _alunoNome.contains('não encontrado') || _alunoNome.contains('inválido')
                                  ? Colors.red
                                  : Colors.green,
                            ),
                            overflow: TextOverflow.ellipsis,
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 10),
                    Row(
                      children: [
                        const Text('Prova ID: '),
                        Expanded(
                          child: TextField(
                            controller: _provaIdController,
                            keyboardType: TextInputType.number,
                            onChanged: (_) => _loadAlunoAndProvaDetails(), // Recarregar ao mudar ID
                            decoration: const InputDecoration(
                              hintText: 'Digite o ID da Prova',
                              border: OutlineInputBorder(),
                              isDense: true,
                            ),
                          ),
                        ),
                        const SizedBox(width: 8),
                        Expanded(
                          flex: 2,
                          child: Text(
                            _provaNome,
                            style: TextStyle(
                              fontWeight: FontWeight.bold,
                              color: _provaNome.contains('não encontrada') || _provaNome.contains('inválido')
                                  ? Colors.red
                                  : Colors.green,
                            ),
                            overflow: TextOverflow.ellipsis,
                          ),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
            ),
            Text('Respostas Lidas:', style: Theme.of(context).textTheme.titleMedium),
            const SizedBox(height: 10),
            Expanded(
              child: ListView.builder(
                itemCount: _respostas.length,
                itemBuilder: (context, index) {
                  final resposta = _respostas[index];
                  return Card(
                    margin: const EdgeInsets.symmetric(vertical: 4),
                    child: Padding(
                      padding: const EdgeInsets.all(8.0),
                      child: Row(
                        children: [
                          Text('Questão ${resposta['questao']}: '),
                          Expanded(
                            child: TextField(
                              controller: TextEditingController(text: resposta['resposta']),
                              onChanged: (newValue) {
                                setState(() {
                                  _respostas[index]['resposta'] = newValue.toUpperCase();
                                });
                              },
                              decoration: const InputDecoration(
                                border: OutlineInputBorder(),
                                contentPadding: EdgeInsets.symmetric(horizontal: 10, vertical: 8),
                              ),
                              textAlign: TextAlign.center,
                              maxLength: 1,
                              textCapitalization: TextCapitalization.characters,
                            ),
                          ),
                        ],
                      ),
                    ),
                  );
                },
              ),
            ),
            const SizedBox(height: 20),
            // Botões de ação
            _isSaving
                ? const Center(child: CirculoEspera())
                : Column(
              children: [
                // Botão principal para salvar diretamente
                ElevatedButton.icon(
                  onPressed: _isLoading || _isSaving ? null : _salvarGabarito,
                  icon: const Icon(Icons.save),
                  label: const Text('Salvar Gabarito'),
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Colors.green,
                    foregroundColor: Colors.white,
                    minimumSize: const Size(double.infinity, 50),
                  ),
                ),
                const SizedBox(height: 12),
                // Botão secundário para preencher manualmente
                OutlinedButton.icon(
                  onPressed: _isLoading || _isSaving ? null : _confirmarEDevolver,
                  icon: const Icon(Icons.edit),
                  label: const Text('Preencher Manualmente'),
                  style: OutlinedButton.styleFrom(
                    minimumSize: const Size(double.infinity, 50),
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}