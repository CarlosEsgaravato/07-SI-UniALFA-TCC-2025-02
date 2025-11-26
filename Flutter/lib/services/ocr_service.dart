// lib/services/ocr_service.dart
import 'dart:convert';
import 'dart:io';
import 'package:http/http.dart' as http;
import 'package:flutter/foundation.dart';

class OcrService {
  final String _openAiApiKey = '';

  Future<Map<String, dynamic>> processGabarito(String imagePath) async {
    if (_openAiApiKey.isEmpty || _openAiApiKey.contains('NOVA_CHAVE')) {
      throw Exception('Configure sua chave da OpenAI no arquivo ocr_service.dart');
    }

    try {
      if (kDebugMode) {
        print('=== INICIANDO OCR: MÉTODO DE FATIAMENTO VERTICAL ===');
      }

      final String imageBase64 = await _encodeImageBase64(imagePath);
      final payload = _buildOpenAiPayload(imageBase64);

      final uri = Uri.parse('https://api.openai.com/v1/chat/completions');
      final headers = {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer $_openAiApiKey',
      };

      final response = await http.post(
        uri,
        headers: headers,
        body: jsonEncode(payload),
      );

      if (response.statusCode == 200) {
        final responseBody = jsonDecode(utf8.decode(response.bodyBytes));
        final String content = responseBody['choices'][0]['message']['content'];

        final List<Map<String, String>> respostas = _parseTokenizedResponse(content);

        return {
          "respostas": respostas,
          "metadados": {
            "metodo": "vertical-slicing-tokenization",
            "status": "sucesso"
          }
        };
      } else {
        throw Exception('Erro OpenAI (${response.statusCode}): ${response.body}');
      }
    } catch (e) {
      if (kDebugMode) print('Erro OCR: $e');
      rethrow;
    }
  }

  Future<String> _encodeImageBase64(String imagePath) async {
    final bytes = await File(imagePath).readAsBytes();
    return base64Encode(bytes);
  }

  Map<String, dynamic> _buildOpenAiPayload(String imageBase64) {

    const String systemPrompt =
        'Você é um leitor de gabaritos OMR de alta precisão.\n'
        'Sua tarefa é converter a imagem visual em uma sequência de tokens de estado.';

    const String userPrompt =
        'Analise a imagem da grade de respostas (Linhas 1-6).\n\n'
        'VISUALIZAÇÃO:\n'
        'Cada linha horizontal possui 6 elementos visuais distintos:\n'
        '1. O NÚMERO da questão (à esquerda).\n'
        '2. A bolinha da coluna A.\n'
        '3. A bolinha da coluna B.\n'
        '4. A bolinha da coluna C.\n'
        '5. A bolinha da coluna D.\n'
        '6. A bolinha da coluna E.\n\n'
        'TAREFA CRÍTICA:\n'
        'Para cada linha de 1 a 6, classifique cada um dos 6 elementos usando estes códigos:\n'
        '- "N" : Para o NÚMERO impresso (sempre o primeiro elemento).\n'
        '- "O" : Para bolinha VAZIA (círculo com centro branco).\n'
        '- "X" : Para bolinha PREENCHIDA (centro escuro/azul).\n\n'
        'IMPORTANTE SOBRE A QUESTÃO 6:\n'
        'O número "6" é redondo e parece uma bolinha. Você DEVE classificá-lo como "N" (Número) e ignorá-lo. A resposta "A" é a bolinha seguinte.\n\n'
        'FORMATO DE SAÍDA (JSON):\n'
        'Retorne um JSON com a string de tokens para cada linha.\n'
        'Exemplo:\n'
        '{ "linhas": [\n'
        '  {"q": 1, "tokens": "N X O O O O"},  (Significa: Num, A=Marcado, Resto=Vazio)\n'
        '  {"q": 2, "tokens": "N O X O O O"}   (Significa: Num, A=Vazio, B=Marcado...)\n'
        '] }';

    return {
      'model': 'gpt-4o',
      'temperature': 0,
      'response_format': { "type": "json_object" },
      'messages': [
        {'role': 'system', 'content': systemPrompt},
        {
          'role': 'user',
          'content': [
            {'type': 'text', 'text': userPrompt},
            {
              'type': 'image_url',
              'image_url': {
                'url': 'data:image/jpeg;base64,$imageBase64',
                'detail': 'high'
              },
            }
          ],
        }
      ],
      'max_tokens': 1000,
    };
  }

  List<Map<String, String>> _parseTokenizedResponse(String content) {
    if (kDebugMode) {
      print('Resposta Tokenizada: $content');
    }

    try {
      final Map<String, dynamic> decoded = jsonDecode(content);
      final List<dynamic> linhas = decoded['linhas'];
      final List<Map<String, String>> resultados = [];

      for (var item in linhas) {
        String tokensRaw = item['tokens'].toString().toUpperCase();
        String tokens = tokensRaw.replaceAll(' ', '');
        String mapDasBolinhas = tokens;

        if (tokens.startsWith('N')) {
          mapDasBolinhas = tokens.substring(1);
        } else if (tokens.length == 6) {
          mapDasBolinhas = tokens.substring(1);
        }

        int indexMarcado = mapDasBolinhas.indexOf('X');

        if (indexMarcado != -1 && indexMarcado < 5) {
          final letras = ['A', 'B', 'C', 'D', 'E'];
          resultados.add({
            'questao': item['q'].toString(),
            'resposta': letras[indexMarcado]
          });
        }
      }
      return resultados;

    } catch (e) {
      print('Erro ao processar tokens: $e');
      return [];
    }
  }

  void dispose() {}
}