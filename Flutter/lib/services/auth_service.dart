import 'dart:convert';
import 'package:flutter/foundation.dart'; // Para o kDebugMode
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';

class AuthService {
  // --- A MUDANÇA MAIS IMPORTANTE ---
  // Usamos 10.0.2.2 para o emulador Android acessar o localhost do seu computador.
  // Se estiver testando no Chrome (web), você pode voltar para 'localhost'.
  static final String _baseUrl = kIsWeb
      ? 'http://localhost:8080/api' // Para Web
      : 'http://10.0.2.2:8080/api'; // Para Android

  // Chaves para salvar os dados no dispositivo
  static const String _keyToken = 'auth_token';
  static const String _keyLogado = 'user_logged_in';
  static const String _keyUserId = 'user_id';
  static const String _keyUsername = 'user_username';
  static const String _keyUserNome = 'user_nome';
  static const String _keyUserEmail = 'user_email';
  static const String _keyUserTipo = 'user_tipo';

  /// Tenta autenticar o usuário na API com ID e senha.
  /// Retorna `true` se o login for bem-sucedido, `false` caso contrário.
  Future<bool> autenticar(String id, String password) async {
    final url = Uri.parse('$_baseUrl/auth/login');

    try {
      final response = await http.post(
        url,
        headers: <String, String>{
          'Content-Type': 'application/json; charset=UTF-8',
        },
        // Envia o corpo da requisição no formato JSON esperado pela API
        body: jsonEncode(<String, String>{
          'id': id,
          'password': password,
        }),
      );

      if (response.statusCode == 200) {
        // Se a API retornar 200 OK, decodifica o JSON
        final Map<String, dynamic> responseData = jsonDecode(response.body);
        // Salva os dados do usuário no dispositivo
        await _salvarDadosUsuario(responseData);
        if (kDebugMode) {
          print('Login bem-sucedido! Token: ${responseData['token']}');
        }
        return true;
      } else {
        // Se a API retornar um erro (ex: 401, 403)
        if (kDebugMode) {
          print('Falha na autenticação: ${response.statusCode} - ${response.body}');
        }
        return false;
      }
    } catch (e) {
      // Captura erros de rede (ex: servidor offline, sem conexão)
      if (kDebugMode) {
        print('Erro de conexão ao tentar autenticar: $e');
      }
      return false;
    }
  }

  /// Salva os dados do usuário recebidos da API no SharedPreferences.
  Future<void> _salvarDadosUsuario(Map<String, dynamic> data) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_keyToken, data['token']);
    await prefs.setBool(_keyLogado, true);
    // userId é um número, então usamos setInt
    await prefs.setInt(_keyUserId, data['userId']);
    await prefs.setString(_keyUsername, data['username']);
    await prefs.setString(_keyUserNome, data['nome']);
    await prefs.setString(_keyUserEmail, data['email']);
    await prefs.setString(_keyUserTipo, data['tipo']);
  }

  /// Realiza o logout do usuário, limpando todos os dados salvos.
  Future<void> logout() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.clear(); // Um jeito mais simples de limpar tudo
    if (kDebugMode) {
      print('Usuário deslogado, dados limpos.');
    }
  }

  /// Verifica se o usuário já está logado.
  Future<bool> isLogado() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getBool(_keyLogado) ?? false;
  }

  /// Retorna o token de autenticação salvo, se existir.
  Future<String?> getToken() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString(_keyToken);
  }
}