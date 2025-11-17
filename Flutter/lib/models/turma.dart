import 'package:hackathonflutter/models/questao.dart';
import 'package:hackathonflutter/models/resposta.dart';

class Turma {
  final int id;
  final String nome;
  final int ano;
  final String periodo;
  final int disciplinaId;
  final String disciplinaNome;
  final int professorId;
  final bool ativa;
  final String professorNome;
  final int totalAlunos;
  final String descricao;

  Turma({
    required this.id,
    required this.nome,
    required this.ano,
    required this.periodo,
    required this.disciplinaId,
    required this.disciplinaNome,
    required this.professorId,
    required this.ativa,
    required this.professorNome,
    required this.totalAlunos,
    required this.descricao,
  });

  factory Turma.fromJson(Map<String, dynamic> json) {
    return Turma(
      id: json['id'],
      nome: json['nome'],
      ano: json['ano'],
      periodo: json['periodo'],
      disciplinaId: json['disciplinaId'],
      disciplinaNome: json['disciplinaNome'],
      professorId: json['professorId'],
      ativa: json['ativa'],
      professorNome: json['professorNome'],
      totalAlunos: json['totalAlunos'],
      descricao: json['descricao']?.toString() ?? '',
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'nome': nome,
      'ano': ano,
      'periodo': periodo,
      'disciplinaId': disciplinaId,
      'disciplinaNome': disciplinaNome,
      'professorId': professorId,
      'ativa': ativa,
      'professorNome': professorNome,
      'totalAlunos': totalAlunos,
      'descricao': descricao,
    };
  }
}