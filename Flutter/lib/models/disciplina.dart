// lib/models/disciplina.dart
import 'package:hackathonflutter/models/turma_simples.dart';

class Disciplina {
  final int id;
  final String nome;
  final TurmaSimples turma; // A turma à qual esta disciplina pertence
  final String? professorNome;

  Disciplina({
    required this.id,
    required this.nome,
    required this.turma,
    this.professorNome,
  });

  // A API /api/disciplinas/...
  // deve retornar a entidade Disciplina, que contém 'turma' e 'professor'.
  factory Disciplina.fromJson(Map<String, dynamic> json) {
    return Disciplina(
      id: json['id'],
      nome: json['nome'],
      // O backend precisa aninhar a informação da turma
      turma: TurmaSimples.fromJson(json['turma']),
      // O backend precisa aninhar o nome do professor
      professorNome:
      json['professor'] != null && json['professor']['usuario'] != null
          ? json['professor']['usuario']['nome']
          : 'Não definido',
    );
  }
}