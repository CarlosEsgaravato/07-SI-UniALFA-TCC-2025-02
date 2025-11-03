// lib/models/questao.dart
class Questao {
  final int id;
  final String numero; // <-- CORRIGIDO: Era 'int', agora é 'String'
  final String enunciado;
  final String alternativaA;
  final String alternativaB;
  final String alternativaC;
  final String alternativaD;
  final String alternativaE;
  final String alternativaCorreta;
  // O seu backend tem 'pontuacao' como BigDecimal,
  // vamos tratá-lo como double no Dart.
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
    // Função auxiliar para converter 'pontuacao' (que pode ser int ou double)
    double parsePontuacao(dynamic pontuacaoJson) {
      if (pontuacaoJson is int) {
        return pontuacaoJson.toDouble();
      } else if (pontuacaoJson is double) {
        return pontuacaoJson;
      } else if (pontuacaoJson is String) {
        return double.tryParse(pontuacaoJson) ?? 0.0;
      }
      return 0.0; // Valor padrão
    }

    return Questao(
      id: json['id'],

      // --- A CORREÇÃO PRINCIPAL ---
      // O 'numero' vem como String do backend
      // Usamos '??' para garantir que não seja nulo.
      numero: json['numero'] ?? '?',

      // --- TORNANDO O RESTO SEGURO CONTRA NULOS ---
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