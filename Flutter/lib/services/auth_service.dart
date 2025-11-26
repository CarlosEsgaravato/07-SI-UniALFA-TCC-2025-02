import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';

class AuthService {
  static final String _baseUrl = kIsWeb
      ? 'http://localhost:8080/api' // Para Web
      : 'http://192.168.0.104:8080/api'; // Para Android

  static const String _keyToken = 'auth_token';
  static const String _keyLogado = 'user_logged_in';
  static const String _keyUserId = 'user_id';
  static const String _keyUsername = 'user_username';
  static const String _keyUserNome = 'user_nome';
  static const String _keyUserEmail = 'user_email';
  static const String _keyUserTipo = 'user_tipo';

  Future<String?> autenticar(String id, String password) async {
    final url = Uri.parse('$_baseUrl/auth/login');

    try {
      final response = await http.post(
        url,
        headers: <String, String>{
          'Content-Type': 'application/json; charset=UTF-8',
        },
        body: jsonEncode(<String, String>{
          'id': id,
          'password': password,
        }),
      );

      if (response.statusCode == 200) {
        final Map<String, dynamic> responseData = jsonDecode(response.body);
        await _salvarDadosUsuario(responseData);
        if (kDebugMode) {
          print('Login bem-sucedido!');
        }
        return null;
      } else {
        String erro = 'Falha na autenticação. Tente novamente.';

        if (response.body.isNotEmpty) {
          try {
            final Map<String, dynamic> errorData = jsonDecode(response.body);
            if (errorData.containsKey('erro')) {
              erro = errorData['erro'];
            }
          } catch (e) {
            if (kDebugMode) {
              print('Erro ao decodificar JSON de erro: $e');
            }
          }
        }

        if (kDebugMode) {
          print('Falha na autenticação: ${response.statusCode} - $erro');
        }
        return erro;
      }
    } catch (e) {
      if (kDebugMode) {
        print('Erro de conexão ao tentar autenticar: $e');
      }
      return 'Erro de conexão. Verifique sua internet e tente novamente.';
    }
  }

  Future<void> _salvarDadosUsuario(Map<String, dynamic> data) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_keyToken, data['token']);
    await prefs.setBool(_keyLogado, true);
    await prefs.setInt(_keyUserId, data['userId']);
    await prefs.setString(_keyUsername, data['username']);
    await prefs.setString(_keyUserNome, data['nome']);
    await prefs.setString(_keyUserEmail, data['email']);
    await prefs.setString(_keyUserTipo, data['tipo']);
  }

  Future<void> logout() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.clear();
    if (kDebugMode) {
      print('Usuário deslogado, dados limpos.');
    }
  }

  Future<bool> isLogado() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getBool(_keyLogado) ?? false;
  }

  Future<String?> getToken() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString(_keyToken);
  }

  Future<String?> getTipoUsuario() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString(_keyUserTipo);
  }

}