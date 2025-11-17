class Aluno {
  final int id;
  final String nome;
  final String? turma;
  final String email;

  Aluno({
    required this.id,
    required this.nome,
    this.turma,
    required this.email,
  });

  factory Aluno.fromJson(Map<String, dynamic> json) {
    return Aluno(
      id: json['id'],
      nome: json['nome'],
      turma: json['turma'] as String?,
      email: json['email'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'nome': nome,
      'turma': turma,
      'email': email,
    };
  }
}