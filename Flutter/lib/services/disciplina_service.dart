// lib/services/disciplina_service.dart
import 'package:hackathonflutter/models/disciplina.dart';
import 'package:hackathonflutter/services/api_service.dart';

class DisciplinaService {
  final ApiService _apiService;

  DisciplinaService(this._apiService);

  // Chama o endpoint GET /api/disciplinas/me
  Future<List<Disciplina>> buscarMinhasDisciplinas() async {
    try {
      final responseData = await _apiService.get('disciplinas/me');
      return (responseData as List)
          .map((json) => Disciplina.fromJson(json))
          .toList();
    } catch (e) {
      print('Erro ao buscar (minhas) disciplinas: $e');
      throw Exception('Falha ao carregar suas disciplinas: $e');
    }
  }

  // Chama o endpoint GET /api/disciplinas/professor/{id}
  Future<List<Disciplina>> buscarDisciplinasPorProfessor(int professorId) async {
    try {
      final responseData =
      await _apiService.get('disciplinas/professor/$professorId');
      return (responseData as List)
          .map((json) => Disciplina.fromJson(json))
          .toList();
    } catch (e) {
      print('Erro ao buscar disciplinas por professor $professorId: $e');
      throw Exception('Falha ao carregar disciplinas do professor: $e');
    }
  }
}