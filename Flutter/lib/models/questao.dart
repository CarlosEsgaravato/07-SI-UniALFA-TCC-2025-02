class Questao {
  final int id;
  final String numero;
  final String enunciado;
  final String alternativaA;
  final String alternativaB;
  final String alternativaC;
  final String alternativaD;
  final String alternativaE;
  final String alternativaCorreta;
  final double pontuacao;

  Questao({
    required this.id,
    required this.numero,
    required this.enunciado,
    required this.alternativaA,
    required this.alternativaB,
    required this.alternativaC,
    required this.alternativaD,
    required this.alternativaE,
    required this.alternativaCorreta,
    required this.pontuacao,
  });

  factory Questao.fromJson(Map<String, dynamic> json) {
    double parsePontuacao(dynamic pontuacaoJson) {
      if (pontuacaoJson is int) {
        return pontuacaoJson.toDouble();
      } else if (pontuacaoJson is double) {
        return pontuacaoJson;
      } else if (pontuacaoJson is String) {
        return double.tryParse(pontuacaoJson) ?? 0.0;
      }
      return 0.0;
    }

    return Questao(
      id: json['id'],
      numero: json['numero'] ?? '?',
      enunciado: json['enunciado'] ?? 'Enunciado não disponível',
      alternativaA: json['alternativaA'] ?? '',
      alternativaB: json['alternativaB'] ?? '',
      alternativaC: json['alternativaC'] ?? '',
      alternativaD: json['alternativaD'] ?? '',
      alternativaE: json['alternativaE'] ?? '',
      alternativaCorreta: json['alternativaCorreta'] ?? '',
      pontuacao: parsePontuacao(json['pontuacao']),
    );
  }
}