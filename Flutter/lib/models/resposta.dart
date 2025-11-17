class Resposta {
  final String questaoNumero;
  final String alternativaSelecionada;

  Resposta({
    required this.questaoNumero,
    required this.alternativaSelecionada,
  });

  factory Resposta.fromJson(Map<String, dynamic> json) {
    return Resposta(
      questaoNumero: json['questaoNumero']?.toString() ?? '?',
      alternativaSelecionada: json['alternativaSelecionada'] ?? '',
    );
  }
}