class Professor {
  final int id;
  final String nome;
  final String? email;


  Professor({required this.id, required this.nome, this.email});

  factory Professor.fromJson(Map<String, dynamic> json) {
    if (json.containsKey('usuario')) {
      final usuario = json['usuario'];
      return Professor(
        id: usuario['id'],
        nome: usuario['nome'],
        email: usuario['email'],
      );
    } else {
      return Professor(
        id: json['id'],
        nome: json['nome'],
        email: json['email'],
      );
    }
  }
}