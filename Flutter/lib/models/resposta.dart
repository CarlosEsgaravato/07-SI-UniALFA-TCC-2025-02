// lib/models/resposta.dart
class Resposta {
  // ATUALIZADO: 'questaoNumero' deve ser String para ser consistente
  final String questaoNumero;
  final String alternativaSelecionada;

  Resposta({
    required this.questaoNumero,
    required this.alternativaSelecionada,
  });

  factory Resposta.fromJson(Map<String, dynamic> json) {
    return Resposta(
      // Converte para String para garantir (ex: 1 -> "1")
      // e protege contra nulos.
      questaoNumero: json['questaoNumero']?.toString() ?? '?',
      alternativaSelecionada: json['alternativaSelecionada'] ?? '',
    );
  }
}