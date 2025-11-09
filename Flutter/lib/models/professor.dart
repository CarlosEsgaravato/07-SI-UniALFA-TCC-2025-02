class Professor {
  final int id;
  final String nome;
  final String? email;


  Professor({required this.id, required this.nome, this.email});

  factory Professor.fromJson(Map<String, dynamic> json) {
    // Ajuste este factory baseado na resposta REAL da sua API
    if (json.containsKey('usuario')) {
      // Se a API /professores retornar a entidade Professor
      final usuario = json['usuario'];
      return Professor(
        id: usuario['id'], // Usamos o ID do USUÁRIO, que é o que os outros serviços usam
        nome: usuario['nome'],
        email: usuario['email'],
      );
    } else {
      // Se a API /professores retornar uma lista de Usuarios (DTO)
      return Professor(
        id: json['id'],
        nome: json['nome'],
        email: json['email'],
      );
    }
  }
}