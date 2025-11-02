// lib/models/prova.dart
import 'package:hackathonflutter/models/questao.dart';
import 'package:hackathonflutter/models/resposta.dart';

class Prova {
  final int id;
  final String titulo;
  final String disciplinaNome;
  final int turmaId;
  final String descricao;
  final List<Questao>? questoes;
  final List<Resposta>? respostasCorretas;

  Prova({
    required this.id,
    required this.titulo,
    required this.disciplinaNome,
    required this.turmaId,
    required this.descricao,
    this.questoes,
    this.respostasCorretas,
  });

  factory Prova.fromJson(Map<String, dynamic> json) {
    return Prova(
      id: json['id'],
      titulo: json['titulo'],
      disciplinaNome: json['disciplinaNome'],
      turmaId: json['turmaId'],
      descricao: json['descricao'],
      questoes: json['questoes'] != null
          ? (json['questoes'] as List).map((i) => Questao.fromJson(i)).toList()
          : null,
      respostasCorretas: json['respostasCorretas'] != null
          ? (json['respostasCorretas'] as List).map((i) => Resposta.fromJson(i)).toList()
          : null,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'titulo': titulo,
      'disciplinaNome': disciplinaNome,
      'turmaId': turmaId,
      'descricao': descricao,
      'questoes': questoes?.map((e) => e.toJson()).toList(),
      'respostasCorretas': respostasCorretas?.map((e) => e.toJson()).toList(),
    };
  }
}