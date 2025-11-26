import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:hackathonflutter/services/auth_service.dart';
import 'package:flutter/foundation.dart';

class ApiService {

  static final String _baseUrl = kIsWeb
      ? 'http://localhost:8080/api'
      : 'http://192.168.0.105:8080/api';

  final AuthService _authService;

  ApiService(this._authService);

  Future<dynamic> get(String endpoint) async {
    final uri = Uri.parse('$_baseUrl/$endpoint');
    final token = await _authService.getToken();

    final headers = {
      'Content-Type': 'application/json; charset=UTF-8',
      if (token != null) 'Authorization': 'Bearer $token',
    };

    try {
      if (kDebugMode) {
        print('ApiService GET: Request to: $uri');
        print('ApiService GET: Headers: $headers');
      }

      final response = await http.get(uri, headers: headers);

      if (kDebugMode) {
        print('ApiService GET: Response Status: ${response.statusCode}');
        print('ApiService GET: Response Body: ${response.body}');
      }

      if (response.statusCode >= 200 && response.statusCode < 300) {
        if (response.body.isNotEmpty) {
          return jsonDecode(response.body);
        } else {
          return null;
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
        print('ApiService POST: Request Body: ${jsonEncode(data)}');
      }

      final response = await http.post(
        uri,
        headers: headers,
        body: jsonEncode(data),
      );

      if (kDebugMode) {
        print('ApiService POST: Response Status: ${response.statusCode}');
        print('ApiService POST: Response Body: ${response.body}');
      }

      if (response.statusCode >= 200 && response.statusCode < 300) {
        if (response.body.isNotEmpty) {
          return jsonDecode(response.body);
        } else {
          return {'message': 'Requisição bem-sucedida, sem conteúdo de resposta.'};
        }
      } else if (response.statusCode == 401) {
        _authService.logout();
        throw Exception('Não autorizado: Token inválido ou expirado.');
      } else {
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