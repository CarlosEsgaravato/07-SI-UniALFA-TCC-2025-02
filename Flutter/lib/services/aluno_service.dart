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
      // Este endpoint retorna um OBJETO TurmaComAlunosDTO, não uma lista
      final responseData = await _apiService.get('turmas/$turmaId/alunos');

      // 1. Verificamos se a resposta é um Map (o '_JsonMap')
      if (responseData is Map<String, dynamic>) {

        // 2. Acedemos à CHAVE 'alunos' dentro desse objeto
        if (responseData.containsKey('alunos') && responseData['alunos'] is List) {

          // 3. Extraímos a lista de dentro da chave 'alunos'
          final List<dynamic> alunosList = responseData['alunos'];

          // 4. Agora sim, mapeamos a lista
          return alunosList.map((json) => Aluno.fromJson(json)).toList();
        } else {
          // A API respondeu com um objeto, mas sem a lista 'alunos'
          print('Erro ao buscar alunos: chave "alunos" não encontrada na resposta.');
          return [];
        }
      } else {
        // A API não respondeu o que esperávamos
        print('Erro ao buscar alunos: A resposta da API não foi um objeto Map.');
        return [];
      }
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