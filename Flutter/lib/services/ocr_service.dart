import 'package:google_mlkit_text_recognition/google_mlkit_text_recognition.dart';
import 'package:image/image.dart' as img;
import 'dart:io';

class OcrService {
  final TextRecognizer _textRecognizer = TextRecognizer();

  Future<Map<String, dynamic>> processGabarito(String imagePath) async {
    final inputImage = InputImage.fromFilePath(imagePath);
    final RecognizedText recognizedText = await _textRecognizer.processImage(inputImage);

    print('=== ANÁLISE COMPLETA DO OCR ===');
    for (TextBlock block in recognizedText.blocks) {
      print('Block: ${block.text}');
      print('Block bounds: ${block.boundingBox}');
      for (TextLine line in block.lines) {
        print('  Line: ${line.text}');
        print('  Line bounds: ${line.boundingBox}');
        for (TextElement element in line.elements) {
          print('    Element: "${element.text}" - Bounds: ${element.boundingBox}');
        }
      }
      print('---');
    }

    Map<String, dynamic> extractedData = {
      "alunoId": "",
      "provaId": "",
      "respostas": [],
      "metadados": {
        "timestamp": DateTime.now().toIso8601String(),
        "qualidade_ocr": 0.0
      }
    };

    // Coletar todos os elementos de texto com suas posições
    List<Map<String, dynamic>> allElements = [];

    for (TextBlock block in recognizedText.blocks) {
      for (TextLine line in block.lines) {
        for (TextElement element in line.elements) {
          allElements.add({
            'text': element.text.trim(),
            'bounds': element.boundingBox,
            'centerX': (element.boundingBox.left + element.boundingBox.right) / 2,
            'centerY': (element.boundingBox.top + element.boundingBox.bottom) / 2,
            'left': element.boundingBox.left,
            'right': element.boundingBox.right,
            'top': element.boundingBox.top,
            'bottom': element.boundingBox.bottom,
          });
        }
      }
    }

    // Ordenar elementos por posição vertical (Y) e depois horizontal (X)
    allElements.sort((a, b) {
      int yCompare = a['centerY'].compareTo(b['centerY']);
      if (yCompare != 0) return yCompare;
      return a['centerX'].compareTo(b['centerX']);
    });

    print('=== ELEMENTOS ORDENADOS ===');
    for (var elem in allElements) {
      print('Text: "${elem['text']}" - X: ${elem['centerX'].toStringAsFixed(1)}, Y: ${elem['centerY'].toStringAsFixed(1)}');
    }

    // === EXTRAIR ALUNO ID ===
    String? alunoId = _extractFieldValue(allElements, 'aluno');
    if (alunoId != null) {
      extractedData["alunoId"] = alunoId;
      print('Aluno ID encontrado: $alunoId');
    }

    // === EXTRAIR PROVA ID ===
    String? provaId = _extractFieldValue(allElements, 'prova');
    if (provaId != null) {
      extractedData["provaId"] = provaId;
      print('Prova ID encontrado: $provaId');
    }

    // === EXTRAIR RESPOSTAS ===
    List<Map<String, String>> respostas = _extractAnswers(allElements);
    extractedData["respostas"] = respostas;

    print('=== RESULTADO FINAL ===');
    print('Aluno ID: ${extractedData["alunoId"]}');
    print('Prova ID: ${extractedData["provaId"]}');
    print('Respostas: ${extractedData["respostas"]}');

    return extractedData;
  }

  // Extrai o valor de um campo (ex: busca "Aluno" e pega o número próximo)
  String? _extractFieldValue(List<Map<String, dynamic>> elements, String fieldName) {
    // Procurar pelo nome do campo (case-insensitive)
    for (int i = 0; i < elements.length; i++) {
      String text = elements[i]['text'].toLowerCase();

      if (text == fieldName.toLowerCase()) {
        double fieldY = elements[i]['centerY'];
        double fieldRight = elements[i]['right'];

        // Procurar por números na mesma linha (±30 pixels) e à direita
        for (int j = 0; j < elements.length; j++) {
          if (i == j) continue;

          String candidateText = elements[j]['text'];
          double candidateY = elements[j]['centerY'];
          double candidateLeft = elements[j]['left'];

          // Verificar se é um número e está na posição correta
          if (RegExp(r'^\d+$').hasMatch(candidateText)) {
            bool sameRow = (candidateY - fieldY).abs() <= 30;
            bool toTheRight = candidateLeft > fieldRight;

            if (sameRow && toTheRight) {
              return candidateText;
            }
          }
        }
      }
    }
    return null;
  }

  // Extrai as respostas do gabarito
  List<Map<String, String>> _extractAnswers(List<Map<String, dynamic>> elements) {
    Map<int, String> answers = {};

    // Estratégia 1: Procurar padrões "número letra" em elementos adjacentes
    for (int i = 0; i < elements.length - 1; i++) {
      String currentText = elements[i]['text'];
      String nextText = elements[i + 1]['text'];

      // Verificar se o elemento atual é um número e o próximo é uma letra
      if (RegExp(r'^\d+$').hasMatch(currentText) &&
          RegExp(r'^[A-Ea-e]$').hasMatch(nextText)) {

        double currentY = elements[i]['centerY'];
        double nextY = elements[i + 1]['centerY'];
        double currentX = elements[i]['centerX'];
        double nextX = elements[i + 1]['centerX'];

        // Verificar se estão na mesma linha (±20 pixels) e próximos horizontalmente
        bool sameLine = (nextY - currentY).abs() <= 20;
        bool closeHorizontally = (nextX - currentX).abs() <= 100;

        if (sameLine && closeHorizontally) {
          int questionNum = int.parse(currentText);
          String answer = nextText.toUpperCase();
          answers[questionNum] = answer;
          print('Resposta encontrada - Estratégia 1: $questionNum -> $answer');
        }
      }
    }

    // Estratégia 2: Procurar padrões "número letra" na mesma linha de texto
    for (var element in elements) {
      String text = element['text'];

      // Padrão: número seguido de letra (ex: "1A", "2C", "10B")
      RegExp pattern = RegExp(r'(\d+)\s*([A-Ea-e])', caseSensitive: false);
      Iterable<RegExpMatch> matches = pattern.allMatches(text);

      for (var match in matches) {
        int questionNum = int.parse(match.group(1)!);
        String answer = match.group(2)!.toUpperCase();
        answers[questionNum] = answer;
        print('Resposta encontrada - Estratégia 2: $questionNum -> $answer');
      }
    }

    // Estratégia 3: Análise espacial por linhas
    if (answers.isEmpty) {
      answers = _spatialAnswerExtraction(elements);
    }

    // Converter para lista ordenada
    List<Map<String, String>> result = [];
    var sortedKeys = answers.keys.toList()..sort();

    for (int key in sortedKeys) {
      result.add({
        "questao": key.toString(),
        "resposta": answers[key]!
      });
    }

    return result;
  }

  // Análise espacial mais sofisticada para extrair respostas
  Map<int, String> _spatialAnswerExtraction(List<Map<String, dynamic>> elements) {
    Map<int, String> answers = {};

    // Agrupar elementos por linhas (Y similar)
    List<List<Map<String, dynamic>>> lines = [];
    const double lineThreshold = 25.0; // pixels de tolerância para considerar mesma linha

    for (var element in elements) {
      bool addedToLine = false;

      for (var line in lines) {
        if (line.isNotEmpty) {
          double lineY = line.first['centerY'];
          if ((element['centerY'] - lineY).abs() <= lineThreshold) {
            line.add(element);
            addedToLine = true;
            break;
          }
        }
      }

      if (!addedToLine) {
        lines.add([element]);
      }
    }

    // Analisar cada linha
    for (var line in lines) {
      // Ordenar elementos da linha por posição X
      line.sort((a, b) => a['centerX'].compareTo(b['centerX']));

      // Procurar padrão número + letra
      for (int i = 0; i < line.length - 1; i++) {
        String currentText = line[i]['text'];
        String nextText = line[i + 1]['text'];

        if (RegExp(r'^\d+$').hasMatch(currentText) &&
            RegExp(r'^[A-Ea-e]$').hasMatch(nextText)) {

          int questionNum = int.parse(currentText);
          String answer = nextText.toUpperCase();
          answers[questionNum] = answer;
          print('Resposta encontrada - Estratégia 3: $questionNum -> $answer');
        }
      }
    }

    return answers;
  }

  void dispose() {
    _textRecognizer.close();
  }
}