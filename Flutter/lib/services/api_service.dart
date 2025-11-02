// lib/services/api_service.dart
import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:hackathonflutter/services/auth_service.dart'; // Importar AuthService
import 'package:flutter/foundation.dart'; // Para kDebugMode

class ApiService {

  final String _baseUrl = 'http://172.16.2.190:8080/api';

  final AuthService _authService; // Injetar AuthService para obter o token

  ApiService(this._authService); // Construtor para receber AuthService

  // Metodo GET para fazer requisições à API
  Future<dynamic> get(String endpoint) async {
    final uri = Uri.parse('$_baseUrl/$endpoint');
    final token = await _authService.getToken(); // Obter o token de autenticação

    final headers = {
      'Content-Type': 'application/json; charset=UTF-8',
      if (token != null) 'Authorization': 'Bearer $token', // Adicionar token se existir
    };

    try {
      if (kDebugMode) { // Apenas em modo debug
        print('ApiService GET: Request to: $uri');
        print('ApiService GET: Headers: $headers');
      }

      final response = await http.get(uri, headers: headers);

      if (kDebugMode) { // Apenas em modo debug
        print('ApiService GET: Response Status: ${response.statusCode}');
        print('ApiService GET: Response Body: ${response.body}');
      }

      if (response.statusCode >= 200 && response.statusCode < 300) {
        if (response.body.isNotEmpty) {
          return jsonDecode(response.body);
        } else {
          return null; // Retorna nulo se o corpo da resposta estiver vazio (ex: 204 No Content)
        }
      } else if (response.statusCode == 401) {
        _authService.logout();
        throw Exception('Não autorizado: Token inválido ou expirado.');
      } else {
        String errorMessage = 'Falha ao buscar dados de $endpoint: ${response.statusCode}';
        if (response.body.isNotEmpty) {
          try {
            final errorBody = jsonDecode(response.body);
            if (errorBody is Map && errorBody.containsKey('message')) {
              errorMessage += ' - ${errorBody['message']}';
            } else {
              errorMessage += ' - ${response.body}';
            }
          } catch (_) {
            errorMessage += ' - ${response.body}';
          }
        }
        throw Exception(errorMessage);
      }
    } catch (e) {
      if (kDebugMode) {
        print('ApiService GET: Erro na requisição GET para $endpoint: $e');
      }
      throw Exception('Erro de conexão ou ao buscar dados: $e');
    }
  }

  // Metodo POST para fazer requisições à API
  Future<dynamic> post(String endpoint, Map<String, dynamic> data) async {
    final uri = Uri.parse('$_baseUrl/$endpoint');
    final token = await _authService.getToken();

    final headers = {
      'Content-Type': 'application/json; charset=UTF-8',
      if (token != null) 'Authorization': 'Bearer $token',
    };

    try {
      if (kDebugMode) {
        print('ApiService POST: Request to: $uri');
        print('ApiService POST: Headers: $headers');
        print('ApiService POST: Request Body: ${jsonEncode(data)}'); // Muito importante para ver o que está sendo enviado
      }

      final response = await http.post(
        uri,
        headers: headers,
        body: jsonEncode(data), // Garanta que o corpo é codificado para JSON
      );

      if (kDebugMode) {
        print('ApiService POST: Response Status: ${response.statusCode}');
        print('ApiService POST: Response Body: ${response.body}');
      }

      if (response.statusCode >= 200 && response.statusCode < 300) {
        // Se a API retornar um corpo vazio para 204 No Content, não tente decodificar
        if (response.body.isNotEmpty) {
          return jsonDecode(response.body);
        } else {
          // Retorna um mapa de sucesso padrão para 204 ou sem corpo, que o serviço pode interpretar
          return {'message': 'Requisição bem-sucedida, sem conteúdo de resposta.'};
        }
      } else if (response.statusCode == 401) {
        _authService.logout();
        throw Exception('Não autorizado: Token inválido ou expirado.');
      } else {
        // Tenta decodificar o erro da API se houver um corpo
        String errorMessage = 'Falha ao enviar dados para $endpoint: ${response.statusCode}';
        if (response.body.isNotEmpty) {
          try {
            final errorBody = jsonDecode(response.body);
            if (errorBody is Map && errorBody.containsKey('message')) {
              errorMessage += ' - ${errorBody['message']}';
            } else {
              errorMessage += ' - ${response.body}';
            }
          } catch (_) {
            errorMessage += ' - ${response.body}';
          }
        }
        throw Exception(errorMessage);
      }
    } catch (e) {
      if (kDebugMode) {
        print('ApiService POST: Erro na requisição POST para $endpoint: $e');
      }
      throw Exception('Erro de conexão ou ao enviar dados: $e');
    }
  }
}