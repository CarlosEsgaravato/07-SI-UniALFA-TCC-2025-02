// lib/models/questao.dart
class Questao {
  final int numero;
  final Map<String, String> alternativas;
  String? respostaSelecionada; // Pode ser String ou null

  Questao({
    required this.numero,
    required this.alternativas,
    this.respostaSelecionada,
  });

  factory Questao.fromJson(Map<String, dynamic> json) {
    return Questao(
      numero: json['numero'],
      // Alternativas deve vir como um Map no JSON
      alternativas: Map<String, String>.from(json['alternativas']),
      respostaSelecionada: json['respostaSelecionada'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'numero': numero,
      'alternativas': alternativas,
      'respostaSelecionada': respostaSelecionada,
    };
  }
}