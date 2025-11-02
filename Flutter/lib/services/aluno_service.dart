// lib/services/aluno_service.dart
import 'package:hackathonflutter/models/aluno.dart';
import 'package:hackathonflutter/services/api_service.dart'; // Importar o ApiService

class AlunoService {
  final ApiService _apiService; // Adicionar a dependência do ApiService

  // Construtor que recebe o ApiService
  AlunoService(this._apiService);

  // Busca todos os alunos da API
  Future<List<Aluno>> buscarAlunos() async {
    try {
      final responseData = await _apiService.get('alunos'); // Chama o método GET do ApiService
      // Assumindo que a API retorna uma lista de mapas para alunos
      return (responseData as List).map((json) => Aluno.fromJson(json)).toList();
    } catch (e) {
      print('Erro ao buscar alunos: $e');
      // Retorna uma lista vazia em caso de erro, ou você pode relançar a exceção
      return [];
    }
  }

  // Busca alunos por turma da API
  Future<List<Aluno>> buscarAlunosPorTurma(int turmaId) async {
    try {
      // Endpoint para buscar alunos por turma pode variar, aqui um exemplo
      final responseData = await _apiService.get('turmas/$turmaId/alunos');
      return (responseData as List).map((json) => Aluno.fromJson(json)).toList();
    } catch (e) {
      print('Erro ao buscar alunos por turma $turmaId: $e');
      return [];
    }
  }

  // NOVO MÉTODO: Buscar aluno por ID
  Future<Aluno?> buscarAlunoPorId(int alunoId) async {
    try {
      final responseData = await _apiService.get('alunos/$alunoId');
      // Verifica se a resposta não é nula e não é uma lista vazia,
      // pois `get` pode retornar um mapa diretamente se for um único item.
      if (responseData != null && responseData is Map<String, dynamic>) {
        return Aluno.fromJson(responseData);
      }
      return null; // Retorna nulo se o aluno não for encontrado
    } catch (e) {
      print('Erro ao buscar aluno $alunoId: $e');
      return null;
    }
  }
}