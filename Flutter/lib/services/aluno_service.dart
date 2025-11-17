import 'package:hackathonflutter/models/aluno.dart';
import 'package:hackathonflutter/services/api_service.dart';

class AlunoService {
  final ApiService _apiService;

  AlunoService(this._apiService);

  Future<List<Aluno>> buscarAlunos() async {
    try {
      final responseData = await _apiService.get('alunos');
      return (responseData as List).map((json) => Aluno.fromJson(json)).toList();
    } catch (e) {
      print('Erro ao buscar alunos: $e');
      return [];
    }
  }
  Future<List<Aluno>> buscarAlunosPorTurma(int turmaId) async {
    try {
      final responseData = await _apiService.get('turmas/$turmaId/alunos');
      if (responseData is Map<String, dynamic>) {
        if (responseData.containsKey('alunos') && responseData['alunos'] is List) {
          final List<dynamic> alunosList = responseData['alunos'];
          return alunosList.map((json) => Aluno.fromJson(json)).toList();
        } else {
          print('Erro ao buscar alunos: chave "alunos" não encontrada na resposta.');
          return [];
        }
      } else {
        print('Erro ao buscar alunos: A resposta da API não foi um objeto Map.');
        return [];
      }
    } catch (e) {
      print('Erro ao buscar alunos por turma $turmaId: $e');
      return [];
    }
  }

  Future<Aluno?> buscarAlunoPorId(int alunoId) async {
    try {
      final responseData = await _apiService.get('alunos/$alunoId');
      if (responseData != null && responseData is Map<String, dynamic>) {
        return Aluno.fromJson(responseData);
      }
      return null;
    } catch (e) {
      print('Erro ao buscar aluno $alunoId: $e');
      return null;
    }
  }
}