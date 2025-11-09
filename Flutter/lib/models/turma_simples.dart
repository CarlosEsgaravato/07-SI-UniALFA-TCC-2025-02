// lib/models/turma_simples.dart
class TurmaSimples {
  final int id;
  final String nome;

  TurmaSimples({required this.id, required this.nome});

  factory TurmaSimples.fromJson(Map<String, dynamic> json) {
    return TurmaSimples(
      id: json['id'],
      nome: json['nome'],
    );
  }
}