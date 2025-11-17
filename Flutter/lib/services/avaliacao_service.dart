import 'package:hackathonflutter/models/questao.dart';
import 'package:hackathonflutter/models/resposta.dart';
import 'package:hackathonflutter/models/prova.dart';
import 'package:hackathonflutter/models/resposta_simples_dto.dart';
import 'package:hackathonflutter/services/api_service.dart';

class AvaliacaoService {
  final ApiService _apiService;
  AvaliacaoService(this._apiService);
  Future<List<Prova>> buscarProvasPorDisciplina(int disciplinaId) async {
    try {
      final responseData =
      await _apiService.get('provas/disciplina/$disciplinaId');
      return (responseData as List).map((json) => Prova.fromJson(json)).toList();
    } catch (e) {
      print('Erro ao buscar provas por disciplina $disciplinaId: $e');
      return [];
    }
  }

  Future<Prova?> buscarProvaPorId(int provaId) async {
    try {
      // Endpoint para /provas/{id} (assumido)
      final responseData = await _apiService.get('provas/$provaId');
      if (responseData != null && responseData is Map<String, dynamic>) {
        return Prova.fromJson(responseData);
      }
      return null;
    } catch (e) {
      print('Erro ao buscar prova por ID $provaId: $e');
      return null;
    }
  }

  Future<List<Questao>> buscarQuestoesProva(int provaId) async {
    try {
      // Endpoint para /questoes/prova/{id}
      final responseData = await _apiService.get('questoes/prova/$provaId');
      if (responseData is List) {
        return responseData.map((json) => Questao.fromJson(json)).toList();
      }
      return [];
    } catch (e) {
      print('Erro ao buscar questões da prova $provaId: $e');
      return [];
    }
  }

  Future<Set<int>> buscarIdsAlunosComResposta(int provaId) async {
    try {
      // Endpoint para /respostas/prova/{provaId}/alunos (do RespostaAlunoController)
      final responseData = await _apiService.get('respostas/prova/$provaId/alunos');
      if (responseData is List) {
        // Converte a lista de IDs (que podem vir como double ou int) para Set<int>
        return responseData.map<int>((id) => (id as num).toInt()).toSet();
      }
      return {};
    } catch (e) {
      print('Erro ao buscar IDs de alunos com resposta: $e');
      return {};
    }
  }

  Future<List<RespostaSimplesDTO>> buscarRespostasDoAluno(int alunoId, int provaId) async {
    try {
      // Endpoint para /respostas/aluno/{alunoId}/prova/{provaId} (do RespostaAlunoController)
      final responseData = await _apiService.get('respostas/aluno/$alunoId/prova/$provaId');
      if (responseData is List) {
        return responseData.map((json) => RespostaSimplesDTO.fromJson(json)).toList();
      }
      return [];
    } catch (e) {
      print('Erro ao buscar respostas do aluno $alunoId: $e');
      return [];
    }
  }

  Future<Map<String, dynamic>> enviarGabaritoAluno(
      int alunoId, int provaId, List<Map<String, String>> respostas) async {
    try {
      final gabaritoData = {
        'idAluno': alunoId,
        'idProva': provaId,
        'respostas': respostas
            .map((r) => {
          'numeroQuestao': r['questao'],
          'alternativaEscolhida': r['resposta'],
        })
            .toList(),
      };
      final response = await _apiService.post('correcao/corrigir', gabaritoData);
      return response;
    } catch (e) {
      print('Erro ao enviar gabarito do aluno: $e');
      rethrow;
    }
  }
}