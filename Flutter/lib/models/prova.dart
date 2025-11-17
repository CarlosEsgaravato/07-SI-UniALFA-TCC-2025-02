import 'package:hackathonflutter/models/questao.dart';
import 'package:hackathonflutter/models/resposta.dart';

class Prova {
  final int id;
  final String titulo;
  final String descricao;
  final String disciplinaNome;
  final List<Questao> questoes;
  final List<Resposta> respostasCorretas;

  Prova({
    required this.id,
    required this.titulo,
    required this.descricao,
    required this.disciplinaNome,
    this.questoes = const [],
    this.respostasCorretas = const [],
  });

  factory Prova.fromJson(Map<String, dynamic> json) {
    String nomeDisciplina = 'Disciplina não informada';
    if (json['disciplina'] != null && json['disciplina']['nome'] != null) {
      nomeDisciplina = json['disciplina']['nome'];
    }
    else if (json['disciplinaNome'] != null) {
      nomeDisciplina = json['disciplinaNome'];
    }

    return Prova(
      id: json['id'],
      titulo: json['titulo'] ?? 'Título não informado',
      descricao: json['descricao'] ?? 'Sem descrição',
      disciplinaNome: nomeDisciplina,
      questoes: (json['questoes'] as List<dynamic>?)
          ?.map((q) => Questao.fromJson(q))
          .toList() ?? [],

      respostasCorretas: (json['respostasCorretas'] as List<dynamic>?)
          ?.map((r) => Resposta.fromJson(r))
          .toList() ?? [],
    );
  }
}