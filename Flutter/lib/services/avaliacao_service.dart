// lib/services/avaliacao_service.dart
import 'package:hackathonflutter/models/questao.dart';
import 'package:hackathonflutter/models/resposta.dart';
import 'package:hackathonflutter/models/turma.dart';
import 'package:hackathonflutter/models/prova.dart';
import 'package:hackathonflutter/services/api_service.dart'; // Importar o ApiService

class AvaliacaoService {
  final ApiService _apiService; // Adicionar a dependência do ApiService

  // Construtor que recebe o ApiService
  AvaliacaoService(this._apiService);

  Future<List<Turma>> buscarTurmas() async {
    try {
      final responseData = await _apiService.get('turmas');
      return (responseData as List).map((json) => Turma.fromJson(json)).toList();
    } catch (e) {
      print('Erro ao buscar turmas: $e');
      return [];
    }
  }

  Future<List<Prova>> buscarProvasPorTurma(int turmaId) async {
    try {
      final responseData = await _apiService.get('provas/turmas/$turmaId/provas');
      return (responseData as List).map((json) => Prova.fromJson(json)).toList();
    } catch (e) {
      print('Erro ao buscar provas por turma $turmaId: $e');
      return [];
    }
  }

  // NOVO MÉTODO: Buscar uma prova completa por ID (incluindo questões e respostas corretas)
  Future<Prova?> buscarProvaPorId(int provaId) async {
    try {
      // Este endpoint deve retornar uma Prova completa, incluindo questões e respostasCorretas.
      // Exemplo: /api/provas/{id}
      final responseData = await _apiService.get('provas/$provaId'); // <--- Endpoint crucial
      if (responseData != null && responseData is Map<String, dynamic>) {
        return Prova.fromJson(responseData);
      }
      return null;
    } catch (e) {
      print('Erro ao buscar prova por ID $provaId: $e');
      return null;
    }
  }

  // Ajustado: Este método agora delega para buscarProvaPorId,
  // mas o nome mantém a intenção de ser para o gabarito.
  // A API precisa garantir que 'provas/$provaId' traga as respostasCorretas.
  Future<Prova?> buscarGabaritoCorreto(int provaId) async {
    // Reutiliza o método buscarProvaPorId, pois ele deve trazer a prova completa
    // com questões e respostasCorretas, que é o que precisamos para o gabarito.
    return await buscarProvaPorId(provaId);
  }

  // NOVO MÉTODO PARA BUSCAR QUESTÕES DE UMA PROVA (se a API tiver um endpoint separado para isso)
  // Mantive este método para flexibilidade, caso haja um endpoint que retorne SÓ as questões.
  Future<List<Questao>> buscarQuestoesProva(int provaId) async {
    try {
      // Este endpoint deve retornar apenas as questões da prova.
      // Ajuste o endpoint conforme sua API, se for diferente de `provas/$provaId`.
      final responseData = await _apiService.get('provas/$provaId/questoes');
      if (responseData is List) {
        return responseData.map((json) => Questao.fromJson(json)).toList();
      }
      return [];
    } catch (e) {
      print('Erro ao buscar questões da prova $provaId: $e');
      return [];
    }
  }

  Future<Map<String, dynamic>> enviarGabaritoAluno(int alunoId, int provaId, List<Map<String, String>> respostas) async {
    try {
      final gabaritoData = {
        'alunoId': alunoId,
        'provaId': provaId,
        'respostas': respostas.map((r) => {
          'questaoNumero': int.parse(r['questao']!),
          'alternativaSelecionada': r['resposta'],
        }).toList(),
      };
      // Endpoint para enviar as respostas de um aluno
      final response = await _apiService.post('correcao/corrigir', gabaritoData);
      return response;
    } catch (e) {
      print('Erro ao enviar gabarito do aluno: $e');
      rethrow; // Relança a exceção para ser tratada na UI
    }
  }
}