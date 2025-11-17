import 'package:hackathonflutter/models/professor.dart';
import 'package:hackathonflutter/services/api_service.dart';

class ProfessorService {
  final ApiService _apiService;

  ProfessorService(this._apiService);

  Future<List<Professor>> buscarProfessores() async {
    try {
      final responseData = await _apiService.get('professores');
      return (responseData as List)
          .map((json) => Professor.fromJson(json))
          .toList();
    } catch (e) {
      print('Erro ao buscar professores: $e');
      throw Exception('Falha ao carregar professores: $e');
    }
  }
}