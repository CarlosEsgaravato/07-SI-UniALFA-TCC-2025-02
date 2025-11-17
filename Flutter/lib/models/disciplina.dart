import 'package:hackathonflutter/models/turma_simples.dart';

class Disciplina {
  final int id;
  final String nome;
  final TurmaSimples turma;
  final String? professorNome;

  Disciplina({
    required this.id,
    required this.nome,
    required this.turma,
    this.professorNome,
  });

  factory Disciplina.fromJson(Map<String, dynamic> json) {
    return Disciplina(
      id: json['id'],
      nome: json['nome'],
      turma: TurmaSimples.fromJson(json['turma']),
      professorNome:
      json['professor'] != null && json['professor']['usuario'] != null
          ? json['professor']['usuario']['nome']
          : 'Não definido',
    );
  }
}