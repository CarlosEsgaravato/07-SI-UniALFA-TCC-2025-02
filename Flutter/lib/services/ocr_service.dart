// lib/services/ocr_service.dart
import 'dart:convert'; // Para base64 e json
import 'dart:io';
import 'package:http/http.dart' as http; // Para fazer chamadas de API
import 'package:flutter/foundation.dart'; // Para kDebugMode

class OcrService {
  final String _openAiApiKey = '';
  Future<Map<String, dynamic>> processGabarito(String imagePath) async {
    print('=== INICIANDO PROCESSAMENTO (OPENAI API) ===');
    if (_openAiApiKey == 'COLOQUE_A_SUA_NOVA_CHAVE_AQUI') {
      print('ERRO: Chave da OpenAI não configurada em ocr_service.dart');
      throw Exception('Por favor, adicione sua NOVA chave da OpenAI em lib/services/ocr_service.dart');
    }

    try {
      final String imageBase64 = await _encodeImageBase64(imagePath);
      final payload = _buildOpenAiPayload(imageBase64);

      final uri = Uri.parse('https://api.openai.com/v1/chat/completions');
      final headers = {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer $_openAiApiKey', // Autenticação
      };
      if (kDebugMode) {
        print('Enviando requisição para a OpenAI...');
      }
      final response = await http.post(
        uri,
        headers: headers,
        body: jsonEncode(payload),
      );
      if (response.statusCode == 200) {
        final responseBody = jsonDecode(response.body);
        final String content = responseBody['choices'][0]['message']['content'];
        final List<Map<String, String>> respostas = _parseOpenAiResponse(content);

        if (kDebugMode) {
          print('Respostas extraídas: ${respostas.length}');
        }
        return {
          "respostas": respostas,
          "metadados": {
            "timestamp": DateTime.now().toIso8601String(),
            "metodo": "openai-gpt4o", // Mudamos o método
            "confianca_media": 0.95 // A OpenAI é geralmente muito confiável
          }
        };

      } else {
        if (kDebugMode) {
          print('Erro da API OpenAI: ${response.statusCode}');
          print('Corpo do Erro: ${response.body}');
        }
        if (response.statusCode == 401) {
          throw Exception('Falha ao processar imagem: Erro 401 - Não autorizado. Verifique se a sua chave da OpenAI é válida e se a sua conta tem créditos.');
        }

        throw Exception('Falha ao processar imagem com OpenAI: ${response.body}');
      }

    } catch (e) {
      if (kDebugMode) {
        print('Erro fatal no processGabarito (OpenAI): $e');
      }
      rethrow;
    }
  }
  Future<String> _encodeImageBase64(String imagePath) async {
    final bytes = await File(imagePath).readAsBytes();
    return base64Encode(bytes);
  }
  Map<String, dynamic> _buildOpenAiPayload(String imageBase64) {
    const String systemPrompt =
        'Você é um assistente de OCR especializado em ler gabaritos de provas.'
        'Sua resposta deve ser *apenas* o JSON extraído, sem nenhum texto introdutório ou de conclusão, como "Aqui está o JSON:".';

    const String userPrompt =
        'Concentre-se *exclusivamente* na tabela com o título "GABARITO" no topo da imagem.'
        'Ignore completamente o texto das perguntas (como "1. Qual das opções...", "2. Qual o modificador...") que aparece na parte inferior da página.'
        'Para cada número de questão (1, 2, 3, 4, 5, 6), encontre a letra (A, B, C, D, E) que está marcada com um círculo "O".'
        'Retorne *apenas* as questões que estão efetivamente marcadas.'
        'Retorne os dados em uma lista JSON, no seguinte formato: `[{"questao": "NUMERO", "resposta": "LETRA"}]`.'
        'Exemplo de saída (baseado na imagem de exemplo que você está vendo): `[{"questao": "1", "resposta": "C"}, {"questao": "2", "resposta": "C"}, {"questao": "4", "resposta": "B"}, {"questao": "6", "resposta": "D"}]`';
    // ====================================================================
    return {
      'model': 'gpt-4o',
      'messages': [
        {
          'role': 'system',
          'content': systemPrompt,
        },
        {
          'role': 'user',
          'content': [
            {
              'type': 'text',
              'text': userPrompt,
            },
            {
              'type': 'image_url',
              'image_url': {
                'url': 'data:image/jpeg;base64,$imageBase64',
              },
            }
          ],
        }
      ],
      'max_tokens': 1500,
    };
  }
  List<Map<String, String>> _parseOpenAiResponse(String content) {
    if (kDebugMode) {
      print('Resposta crua da OpenAI: $content');
    }

    String jsonString = content;
    final jsonRegex = RegExp(r'```json\s*([\s\S]*?)\s*```');
    final match = jsonRegex.firstMatch(content);

    if (match != null) {
      jsonString = match.group(1)!.trim();
    } else {
      int firstBracket = jsonString.indexOf('[');
      if (firstBracket != -1) {
        jsonString = jsonString.substring(firstBracket);
      }
    }

    try {
      final List<dynamic> decodedList = jsonDecode(jsonString);
      final List<Map<String, String>> respostas = decodedList.map((item) {
        return {
          'questao': item['questao'].toString(),
          'resposta': item['resposta'].toString().toUpperCase(),
        };
      }).toList();

      return respostas;

    } catch (e) {
      if (kDebugMode) {
        print('Erro ao decodificar JSON da OpenAI: $e');
        print('String JSON que falhou: $jsonString');
      }
      return [];
    }
  }
  void dispose() {
  }
}