import 'package:flutter/material.dart';
import 'package:hackathonflutter/models/aluno.dart';
import 'package:hackathonflutter/models/prova.dart';
import 'package:hackathonflutter/models/disciplina.dart';
import 'package:hackathonflutter/models/professor.dart';
import 'package:hackathonflutter/services/aluno_service.dart';
import 'package:hackathonflutter/services/avaliacao_service.dart';
import 'package:hackathonflutter/services/disciplina_service.dart';
import 'package:hackathonflutter/services/professor_service.dart';
import 'package:hackathonflutter/ui/pages/gabarito_page.dart';
import 'package:hackathonflutter/ui/widgets/circulo_espera.dart';
import 'package:hackathonflutter/ui/widgets/msg_alerta.dart';
import 'package:provider/provider.dart';

enum ListagemModo { Professor, Admin }
enum ListagemView { Professores, Disciplinas, Provas, Alunos }

class ListagemPage extends StatefulWidget {
  final bool isViewingGabarito;
  final ListagemModo modo;

  const ListagemPage({
    super.key,
    this.isViewingGabarito = false,
    required this.modo,
  });

  @override
  State<ListagemPage> createState() => _ListagemPageState();
}

class _ListagemPageState extends State<ListagemPage> {
  late AvaliacaoService _avaliacaoService;
  late AlunoService _alunoService;
  late ProfessorService _professorService;
  late DisciplinaService _disciplinaService;
  late ListagemView _currentView;
  String _appBarTitle = 'Carregando...';
  bool _carregando = true;
  List<Professor> _professores = [];
  List<Disciplina> _disciplinas = [];
  List<Prova> _provas = [];
  List<Aluno> _alunos = [];
  Set<int> _alunosComResposta = {};
  Professor? _professorSelecionado;
  Disciplina? _disciplinaSelecionada;
  Prova? _provaSelecionada;

  @override
  void initState() {
    super.initState();
    _avaliacaoService = Provider.of<AvaliacaoService>(context, listen: false);
    _alunoService = Provider.of<AlunoService>(context, listen: false);
    _professorService = Provider.of<ProfessorService>(context, listen: false);
    _disciplinaService = Provider.of<DisciplinaService>(context, listen: false);

    if (widget.modo == ListagemModo.Admin) {
      _currentView = ListagemView.Professores;
      _appBarTitle = 'Professores';
      _carregarProfessores();
    } else {
      _currentView = ListagemView.Disciplinas;
      _appBarTitle = 'Minhas Disciplinas';
      _carregarMinhasDisciplinas();
    }
  }

  Future<void> _carregarProfessores() async {
    setState(() { _carregando = true; });
    try {
      _professores = await _professorService.buscarProfessores();
    } catch (e) {
      if (mounted) { MsgAlerta.showError(context, 'Erro', 'Erro ao carregar professores: $e'); }
    } finally {
      if (mounted) { setState(() { _carregando = false; }); }
    }
  }

  Future<void> _carregarMinhasDisciplinas() async {
    setState(() { _carregando = true; });
    try {
      _disciplinas = await _disciplinaService.buscarMinhasDisciplinas();
    } catch (e) {
      if (mounted) { MsgAlerta.showError(context, 'Erro', 'Erro ao carregar suas disciplinas: $e'); }
    } finally {
      if (mounted) { setState(() { _carregando = false; }); }
    }
  }

  Future<void> _carregarDisciplinasDoProfessor(int professorId) async {
    setState(() { _carregando = true; });
    try {
      _disciplinas = await _disciplinaService.buscarDisciplinasPorProfessor(professorId);
      _appBarTitle = 'Disciplinas de ${_professorSelecionado?.nome ?? ''}';
      _currentView = ListagemView.Disciplinas;
    } catch (e) {
      if (mounted) { MsgAlerta.showError(context, 'Erro', 'Erro ao carregar disciplinas: $e'); }
    } finally {
      if (mounted) { setState(() { _carregando = false; }); }
    }
  }

  Future<void> _carregarProvas(int disciplinaId) async {
    setState(() { _carregando = true; });
    try {
      _provas = await _avaliacaoService.buscarProvasPorDisciplina(disciplinaId);
      _appBarTitle = 'Provas';
      _currentView = ListagemView.Provas;
    } catch (e) {
      if (mounted) { MsgAlerta.showError(context, 'Erro', 'Erro ao carregar provas: $e'); }
    } finally {
      if (mounted) { setState(() { _carregando = false; }); }
    }
  }

  // MÉTODO ATUALIZADO (para buscar quem já respondeu)
  Future<void> _carregarAlunos(int turmaId) async {
    setState(() {
      _carregando = true;
      _alunosComResposta.clear();
    });

    try {
      final alunosPromise = _alunoService.buscarAlunosPorTurma(turmaId);
      final respostasPromise = _avaliacaoService.buscarIdsAlunosComResposta(_provaSelecionada!.id);
      final results = await Future.wait([alunosPromise, respostasPromise]);

      _alunos = results[0] as List<Aluno>;
      _alunosComResposta = results[1] as Set<int>;

      _appBarTitle = 'Alunos';
      _currentView = ListagemView.Alunos;

    } catch (e) {
      if (mounted) { MsgAlerta.showError(context, 'Erro', 'Erro ao carregar alunos e respostas: $e'); }
    } finally {
      if (mounted) { setState(() { _carregando = false; }); }
    }
  }

  Future<bool> _onBackPressed() async {
    if (_currentView == ListagemView.Alunos) {
      setState(() {
        _currentView = ListagemView.Provas;
        _appBarTitle = 'Provas';
        _provaSelecionada = null;
      });
      return false;
    }
    else if (_currentView == ListagemView.Provas) {
      setState(() {
        _currentView = ListagemView.Disciplinas;
        _appBarTitle = widget.modo == ListagemModo.Admin
            ? 'Disciplinas de ${_professorSelecionado?.nome ?? ''}'
            : 'Minhas Disciplinas';
        _disciplinaSelecionada = null;
      });
      return false;
    }
    else if (_currentView == ListagemView.Disciplinas && widget.modo == ListagemModo.Admin) {
      setState(() {
        _currentView = ListagemView.Professores;
        _appBarTitle = 'Professores';
        _professorSelecionado = null;
      });
      return false;
    }
    return true;
  }

  Widget _buildProfessoresList() {
    if (_professores.isEmpty) {
      return const Center(child: Text('Nenhum professor encontrado.'));
    }
    return ListView.builder(
      padding: const EdgeInsets.all(8),
      itemCount: _professores.length,
      itemBuilder: (context, index) {
        final professor = _professores[index];
        return Card(
          elevation: 3,
          margin: const EdgeInsets.symmetric(horizontal: 8, vertical: 6),
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
          child: ListTile(
            contentPadding: const EdgeInsets.symmetric(vertical: 10, horizontal: 16),
            leading: Icon(
              Icons.person_outline,
              color: Theme.of(context).colorScheme.primary,
              size: 40,
            ),
            title: Text(
              professor.nome,
              style: Theme.of(context).textTheme.titleLarge?.copyWith(fontWeight: FontWeight.bold),
            ),
            subtitle: Text(
              professor.email ?? 'Email não cadastrado',
              style: Theme.of(context).textTheme.bodyMedium,
            ),
            trailing: const Icon(Icons.arrow_forward_ios, size: 16),
            onTap: () {
              setState(() {
                _professorSelecionado = professor;
              });
              _carregarDisciplinasDoProfessor(professor.id);
            },
          ),
        );
      },
    );
  }

  Widget _buildDisciplinasList() {
    if (_disciplinas.isEmpty) {
      return Center(child: Text(
          widget.modo == ListagemModo.Professor
              ? 'Nenhuma disciplina encontrada para você.'
              : 'Nenhuma disciplina encontrada para este professor.'
      ));
    }
    return ListView.builder(
      padding: const EdgeInsets.all(8),
      itemCount: _disciplinas.length,
      itemBuilder: (context, index) {
        final disciplina = _disciplinas[index];
        return Card(
          elevation: 3,
          margin: const EdgeInsets.symmetric(horizontal: 8, vertical: 6),
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
          child: ListTile(
            contentPadding: const EdgeInsets.symmetric(vertical: 10, horizontal: 16),
            leading: Icon(
              Icons.school_outlined,
              color: Theme.of(context).colorScheme.primary,
              size: 40,
            ),
            title: Text(
              disciplina.nome,
              style: Theme.of(context).textTheme.titleLarge?.copyWith(fontWeight: FontWeight.bold),
            ),
            subtitle: Text(
              'Turma: ${disciplina.turma.nome}',
              style: Theme.of(context).textTheme.bodyMedium,
            ),
            trailing: const Icon(Icons.arrow_forward_ios, size: 16),
            onTap: () {
              setState(() {
                _disciplinaSelecionada = disciplina;
              });
              _carregarProvas(disciplina.id);
            },
          ),
        );
      },
    );
  }

  Widget _buildProvasList() {
    if (_provas.isEmpty) {
      return const Center(child: Text('Nenhuma prova encontrada para esta disciplina.'));
    }
    return ListView.builder(
      padding: const EdgeInsets.all(8),
      itemCount: _provas.length,
      itemBuilder: (context, index) {
        final prova = _provas[index];
        return Card(
          elevation: 3,
          margin: const EdgeInsets.symmetric(horizontal: 8, vertical: 6),
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
          child: ListTile(
            contentPadding: const EdgeInsets.symmetric(vertical: 10, horizontal: 16),
            leading: Icon(
              Icons.article_outlined,
              color: Theme.of(context).colorScheme.secondary,
              size: 40,
            ),
            title: Text(
              prova.titulo,
              style: Theme.of(context).textTheme.titleLarge?.copyWith(fontWeight: FontWeight.bold),
            ),
            subtitle: Text(
              'Descrição: ${prova.descricao}',
              style: Theme.of(context).textTheme.bodyMedium,
            ),
            trailing: const Icon(Icons.arrow_forward_ios, size: 16),
            onTap: () {
              if (widget.isViewingGabarito) {
              } else {
                setState(() {
                  _provaSelecionada = prova;
                });
                if (_disciplinaSelecionada != null) {
                  _carregarAlunos(_disciplinaSelecionada!.turma.id);
                }
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
      padding: const EdgeInsets.all(8),
      itemCount: _alunos.length,
      itemBuilder: (context, index) {
        final aluno = _alunos[index];
        // VERIFICA SE O ALUNO JÁ RESPONDEU
        final bool jaRespondeu = _alunosComResposta.contains(aluno.id);

        return Card(
          elevation: 3,
          margin: const EdgeInsets.symmetric(horizontal: 8, vertical: 6),
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
          child: ListTile(
            contentPadding: const EdgeInsets.symmetric(vertical: 10, horizontal: 16),
            leading: Icon(
              Icons.face_outlined,
              color: Theme.of(context).colorScheme.secondary,
              size: 40,
            ),
            title: Text(
              aluno.nome,
              style: Theme.of(context).textTheme.titleLarge?.copyWith(fontWeight: FontWeight.bold),
            ),
            subtitle: Text(
              'Email: ${aluno.email}',
              style: Theme.of(context).textTheme.bodyMedium,
            ),
            trailing: jaRespondeu
                ? Chip(
              label: Text('Enviado', style: TextStyle(color: Colors.white, fontSize: 12)),
              backgroundColor: Colors.green,
              padding: EdgeInsets.symmetric(horizontal: 4, vertical: 0),
              labelPadding: EdgeInsets.symmetric(horizontal: 4.0),
            )
                : const Icon(Icons.arrow_forward_ios, size: 16),

            onTap: () {
              if (_provaSelecionada != null) {
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (context) => GabaritoPage(
                      aluno: aluno,
                      prova: _provaSelecionada!,
                      isViewingGabarito: false,
                    ),
                  ),
                ).then((_) {
                  if (_provaSelecionada != null && _disciplinaSelecionada != null) {
                    _carregarAlunos(_disciplinaSelecionada!.turma.id);
                  }
                });
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
          leading: IconButton(
            icon: const Icon(Icons.arrow_back),
            onPressed: () async {
              final shouldPop = await _onBackPressed();
              if (shouldPop && mounted) {
                Navigator.of(context).pop();
              }
            },
          ),
          backgroundColor: Theme.of(context).primaryColor,
          foregroundColor: Colors.white,
        ),
        body: _carregando
            ? const CirculoEspera()
            : Builder(
          builder: (BuildContext context) {
            if (_currentView == ListagemView.Professores) {
              return _buildProfessoresList();
            } else if (_currentView == ListagemView.Disciplinas) {
              return _buildDisciplinasList();
            } else if (_currentView == ListagemView.Provas) {
              return _buildProvasList();
            } else if (_currentView == ListagemView.Alunos) {
              return _buildAlunosList();
            }
            return const Center(child: Text('Estado de visualização desconhecido.'));
          },
        ),
      ),
    );
  }
}