// lib/ui/pages/listagem_page.dart
import 'package:flutter/material.dart';
import 'package:hackathonflutter/models/aluno.dart';
import 'package:hackathonflutter/models/prova.dart';
import 'package:hackathonflutter/models/turma.dart';
import 'package:hackathonflutter/services/aluno_service.dart';
import 'package:hackathonflutter/services/avaliacao_service.dart';
import 'package:hackathonflutter/ui/pages/gabarito_page.dart';
import 'package:hackathonflutter/ui/widgets/circulo_espera.dart';
import 'package:hackathonflutter/ui/widgets/msg_alerta.dart';
import 'package:provider/provider.dart'; // Importar o pacote provider

// Enum para gerenciar as diferentes visualizações da página
enum ListagemView { turmas, provas, alunos }

class ListagemPage extends StatefulWidget {
  final bool isViewingGabarito; // Novo parâmetro: indica se o objetivo é ver gabaritos
  final Aluno? alunoSelecionado; // Adicionado para passar para o GabaritoPage se necessário

  const ListagemPage({
    super.key,
    this.isViewingGabarito = false, // Valor padrão false para alunos
    this.alunoSelecionado,
  });

  @override
  State<ListagemPage> createState() => _ListagemPageState();
}

class _ListagemPageState extends State<ListagemPage> {
  late AvaliacaoService _avaliacaoService;
  late AlunoService _alunoService;

  ListagemView _currentView = ListagemView.turmas;
  String _appBarTitle = 'Turmas';
  bool _carregando = true;

  List<Turma> _turmas = [];
  List<Prova> _provas = [];
  List<Aluno> _alunos = [];

  Turma? _turmaSelecionada; // Armazena a turma selecionada
  Prova? _provaSelecionada; // Armazena a prova selecionada para o aluno

  @override
  void initState() {
    super.initState();
    _avaliacaoService = Provider.of<AvaliacaoService>(context, listen: false);
    _alunoService = Provider.of<AlunoService>(context, listen: false);
    if (widget.isViewingGabarito) {
      _appBarTitle = 'Gabaritos das Provas'; // Ajusta o título para gabarito
    }
    _carregarTurmas();
  }

  Future<void> _carregarTurmas() async {
    setState(() {
      _carregando = true;
    });
    try {
      _turmas = await _avaliacaoService.buscarTurmas();
    } catch (e) {
      if (mounted) {
        MsgAlerta.showError(context, 'Erro', 'Erro ao carregar turmas: $e');
      }
    } finally {
      setState(() {
        _carregando = false;
      });
    }
  }

  Future<void> _carregarProvas(int turmaId) async {
    setState(() {
      _carregando = true;
    });
    try {
      _provas = await _avaliacaoService.buscarProvasPorTurma(turmaId);
      _appBarTitle = 'Provas';
      _currentView = ListagemView.provas;
    } catch (e) {
      if (mounted) {
        MsgAlerta.showError(context, 'Erro', 'Erro ao carregar provas: $e');
      }
    } finally {
      setState(() {
        _carregando = false;
      });
    }
  }

  Future<void> _carregarAlunos(int turmaId) async {
    setState(() {
      _carregando = true;
    });
    try {
      _alunos = await _alunoService.buscarAlunosPorTurma(turmaId);
      _appBarTitle = 'Alunos';
      _currentView = ListagemView.alunos;
    } catch (e) {
      if (mounted) {
        MsgAlerta.showError(context, 'Erro', 'Erro ao carregar alunos: $e');
      }
    } finally {
      setState(() {
        _carregando = false;
      });
    }
  }

  // Método para lidar com o botão de voltar customizado
  Future<bool> _onBackPressed() async {
    if (_currentView == ListagemView.provas) {
      setState(() {
        _currentView = ListagemView.turmas;
        _appBarTitle = 'Turmas';
        _turmaSelecionada = null;
      });
      return false; // Não permite o pop padrão do Flutter
    } else if (_currentView == ListagemView.alunos) {
      setState(() {
        _currentView = ListagemView.provas;
        _appBarTitle = 'Provas';
        _provaSelecionada = null; // Reseta a prova selecionada ao voltar dos alunos
      });
      return false; // Não permite o pop padrão do Flutter
    }
    return true; // Permite o pop padrão se estiver na ListagemView.turmas
  }

  Widget _buildTurmasList() {
    if (_turmas.isEmpty) {
      return const Center(child: Text('Nenhuma turma encontrada.'));
    }
    return ListView.builder(
      itemCount: _turmas.length,
      itemBuilder: (context, index) {
        final turma = _turmas[index];
        return Card(
          margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
          elevation: 4,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
          child: ListTile(
            contentPadding: const EdgeInsets.all(16),
            title: Text(
              turma.nome,
              style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 18),
            ),
            subtitle: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text('Ano: ${turma.ano} - Período: ${turma.periodo}'),
                Text('Disciplina: ${turma.disciplinaNome}'),
                Text('Professor: ${turma.professorNome}'),
              ],
            ),
            trailing: const Icon(Icons.arrow_forward_ios),
            onTap: () {
              setState(() {
                _turmaSelecionada = turma;
              });
              _carregarProvas(turma.id);
            },
          ),
        );
      },
    );
  }

  Widget _buildProvasList() {
    if (_provas.isEmpty) {
      return const Center(child: Text('Nenhuma prova encontrada para esta turma.'));
    }
    return ListView.builder(
      itemCount: _provas.length,
      itemBuilder: (context, index) {
        final prova = _provas[index];
        return Card(
          margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
          elevation: 4,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
          child: ListTile(
            contentPadding: const EdgeInsets.all(16),
            title: Text(
              prova.titulo,
              style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 18),
            ),
            subtitle: Text(
              'Disciplina: ${prova.disciplinaNome}\nDescrição: ${prova.descricao}',
            ),
            trailing: const Icon(Icons.arrow_forward_ios),
            onTap: () {
              // Lógica CONDICIONAL baseada em isViewingGabarito
              if (widget.isViewingGabarito) {
                // Se o objetivo é ver o gabarito, navega para a GabaritoPage
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (context) => GabaritoPage(
                      prova: prova, // Passa a prova (que terá o ID)
                      isViewingGabarito: true, // Indica que é para visualizar o gabarito
                    ),
                  ),
                );
              } else {
                // Se o objetivo é um aluno preencher, continua para a lista de alunos
                setState(() {
                  _provaSelecionada = prova; // Armazena a prova selecionada
                });
                _carregarAlunos(_turmaSelecionada!.id);
              }
            },
          ),
        );
      },
    );
  }

  Widget _buildAlunosList() {
    if (_alunos.isEmpty) {
      return const Center(child: Text('Nenhum aluno encontrado para esta turma.'));
    }
    return ListView.builder(
      itemCount: _alunos.length,
      itemBuilder: (context, index) {
        final aluno = _alunos[index];
        return Card(
          margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
          elevation: 4,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
          child: ListTile(
            contentPadding: const EdgeInsets.all(16),
            title: Text(
              aluno.nome,
              style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 18),
            ),
            subtitle: Text('Email: ${aluno.email}'),
            trailing: const Icon(Icons.arrow_forward_ios),
            onTap: () {
              // Navega para a GabaritoPage para o aluno preencher
              if (_provaSelecionada != null) {
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (context) => GabaritoPage(
                      aluno: aluno,
                      prova: _provaSelecionada!,
                      isViewingGabarito: false, // Indica que é para preencher gabarito
                    ),
                  ),
                );
              } else {
                MsgAlerta.showError(context, 'Erro', 'Nenhuma prova selecionada.');
              }
            },
          ),
        );
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    return PopScope( // Usar PopScope para Flutter 3.12+
      canPop: true, // Permite que o sistema de navegação pop por padrão
      onPopInvoked: (didPop) async {
        if (didPop) return; // Se o pop já ocorreu, não faça nada

        // Chame sua lógica de _onBackPressed
        final shouldPop = await _onBackPressed();
        if (shouldPop && mounted) {
          Navigator.of(context).pop(); // Se _onBackPressed retornar true, permite o pop
        }
      },
      child: Scaffold(
        appBar: AppBar(
          title: Text(_appBarTitle),
          leading: IconButton(
            icon: const Icon(Icons.arrow_back),
            onPressed: () async {
              final shouldPop = await _onBackPressed();
              if (shouldPop && mounted) {
                Navigator.of(context).pop();
              }
            },
          ),
          backgroundColor: Theme.of(context).primaryColor, // Estilo do AppBar
          foregroundColor: Colors.white, // Estilo do AppBar
        ),
        body: _carregando
            ? const CirculoEspera()
            : Builder(
          builder: (BuildContext context) {
            if (_currentView == ListagemView.turmas) {
              return _buildTurmasList();
            } else if (_currentView == ListagemView.provas) {
              return _buildProvasList();
            } else if (_currentView == ListagemView.alunos) {
              return _buildAlunosList();
            }
            // Fallback para qualquer estado inesperado
            return const Center(child: Text('Estado de visualização desconhecido.'));
          },
        ),
      ),
    );
  }
}