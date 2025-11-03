// lib/models/resposta_simples_dto.dart
class RespostaSimplesDTO {
  final String numeroQuestao;
  final String alternativaEscolhida;

  RespostaSimplesDTO({
    required this.numeroQuestao,
    required this.alternativaEscolhida,
  });

  factory RespostaSimplesDTO.fromJson(Map<String, dynamic> json) {
    return RespostaSimplesDTO(
      //
      numeroQuestao: json['numeroQuestao']?.toString() ?? '?',
      alternativaEscolhida: json['alternativaEscolhida'] ?? '',
    );
  }
}