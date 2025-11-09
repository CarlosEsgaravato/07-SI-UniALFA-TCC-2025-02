import 'package:google_mlkit_text_recognition/google_mlkit_text_recognition.dart';
import 'package:image/image.dart' as img;
import 'dart:io';
import 'dart:math';

class CircleDetectionService {
  final TextRecognizer _textRecognizer = TextRecognizer();

  /// Processa o gabarito detectando círculos preenchidos
  Future<Map<String, dynamic>> processGabaritoComCirculos(String imagePath) async {
    print('=== INICIANDO DETECÇÃO DE CÍRCULOS PREENCHIDOS ===');

    // 1. Carregar e processar a imagem
    final imageFile = File(imagePath);
    final bytes = await imageFile.readAsBytes();
    img.Image? image = img.decodeImage(bytes);

    if (image == null) {
      throw Exception('Não foi possível decodificar a imagem');
    }

    // 2. Converter para escala de cinza para melhor análise
    img.Image grayImage = img.grayscale(image);

    // 3. Usar OCR para encontrar os números das questões e texto "ALUNO" e "PROVA"
    final inputImage = InputImage.fromFilePath(imagePath);
    final RecognizedText recognizedText = await _textRecognizer.processImage(inputImage);

    // 4. Extrair IDs
    Map<String, dynamic> extractedData = {
      "alunoId": "",
      "provaId": "",
      "respostas": [],
      "metadados": {
        "timestamp": DateTime.now().toIso8601String(),
        "metodo": "circle_detection",
        "confianca": 0.0
      }
    };

    // Coletar elementos de texto
    List<Map<String, dynamic>> allElements = _extractTextElements(recognizedText);

    // 5. Extrair IDs
    String? alunoId = _extractFieldValue(allElements, 'aluno');
    String? provaId = _extractFieldValue(allElements, 'prova');

    extractedData["alunoId"] = alunoId ?? "";
    extractedData["provaId"] = provaId ?? "";

    print('Aluno ID: ${extractedData["alunoId"]}');
    print('Prova ID: ${extractedData["provaId"]}');

    // 6. Encontrar a região do gabarito
    Map<String, dynamic>? gabaritoRegion = _findGabaritoRegion(allElements, image);

    if (gabaritoRegion == null) {
      print('⚠️ Região do gabarito não encontrada');
      return extractedData;
    }

    print('Região do gabarito encontrada: ${gabaritoRegion}');

    // 7. Detectar círculos preenchidos
    List<Map<String, String>> respostas = await _detectFilledCircles(
        grayImage,
        allElements,
        gabaritoRegion
    );

    extractedData["respostas"] = respostas;
    extractedData["metadados"]["confianca"] = respostas.length > 0 ? 0.85 : 0.0;

    print('=== RESULTADO FINAL ===');
    print('Total de respostas detectadas: ${respostas.length}');
    print('Respostas: $respostas');

    return extractedData;
  }

  /// Extrai elementos de texto com posições
  List<Map<String, dynamic>> _extractTextElements(RecognizedText recognizedText) {
    List<Map<String, dynamic>> elements = [];

    for (TextBlock block in recognizedText.blocks) {
      for (TextLine line in block.lines) {
        for (TextElement element in line.elements) {
          elements.add({
            'text': element.text.trim(),
            'bounds': element.boundingBox,
            'centerX': (element.boundingBox.left + element.boundingBox.right) / 2,
            'centerY': (element.boundingBox.top + element.boundingBox.bottom) / 2,
            'left': element.boundingBox.left,
            'right': element.boundingBox.right,
            'top': element.boundingBox.top,
            'bottom': element.boundingBox.bottom,
            'width': element.boundingBox.right - element.boundingBox.left,
            'height': element.boundingBox.bottom - element.boundingBox.top,
          });
        }
      }
    }

    elements.sort((a, b) {
      int yCompare = (a['centerY'] as double).compareTo(b['centerY'] as double);
      if (yCompare != 0) return yCompare;
      return (a['centerX'] as double).compareTo(b['centerX'] as double);
    });

    return elements;
  }

  /// Extrai valor de campo (ALUNO ID, PROVA ID)
  String? _extractFieldValue(List<Map<String, dynamic>> elements, String fieldName) {
    for (int i = 0; i < elements.length; i++) {
      String text = elements[i]['text'].toLowerCase().replaceAll(RegExp(r'[^a-z]'), '');

      if (text.contains(fieldName.toLowerCase())) {
        double fieldY = elements[i]['centerY'];
        double fieldRight = elements[i]['right'];

        // Procurar números na mesma linha
        for (int j = 0; j < elements.length; j++) {
          if (i == j) continue;

          String candidateText = elements[j]['text'].replaceAll(RegExp(r'[^\d]'), '');
          double candidateY = elements[j]['centerY'];
          double candidateLeft = elements[j]['left'];

          if (candidateText.isNotEmpty && RegExp(r'^\d+$').hasMatch(candidateText)) {
            bool sameRow = (candidateY - fieldY).abs() <= 30;
            bool toTheRight = candidateLeft > fieldRight - 20;

            if (sameRow && toTheRight) {
              return candidateText;
            }
          }
        }
      }
    }
    return null;
  }

  /// Encontra a região do gabarito na imagem
  Map<String, dynamic>? _findGabaritoRegion(List<Map<String, dynamic>> elements, img.Image image) {
    // Procurar pela palavra "GABARITO"
    for (var element in elements) {
      String text = element['text'].toUpperCase();
      if (text.contains('GABARITO')) {
        double top = element['bottom'];
        double left = 50.0;
        double right = image.width.toDouble() - 50;
        double bottom = min(top + 400, image.height.toDouble() - 50);

        return {
          'top': top,
          'left': left,
          'right': right,
          'bottom': bottom,
          'width': right - left,
          'height': bottom - top,
        };
      }
    }
    return null;
  }

  /// Detecta círculos preenchidos na região do gabarito
  Future<List<Map<String, String>>> _detectFilledCircles(
      img.Image grayImage,
      List<Map<String, dynamic>> elements,
      Map<String, dynamic> gabaritoRegion
      ) async {
    print('\n=== DETECTANDO CÍRCULOS PREENCHIDOS ===');

    List<Map<String, String>> respostas = [];

    // Encontrar números de questões na região do gabarito
    List<Map<String, dynamic>> questionNumbers = [];

    for (var element in elements) {
      String text = element['text'].replaceAll(RegExp(r'[^\d]'), '');
      double centerY = element['centerY'];
      double centerX = element['centerX'];

      // Verificar se está na região do gabarito
      bool inRegion = centerY >= gabaritoRegion['top'] &&
          centerY <= gabaritoRegion['bottom'] &&
          centerX >= gabaritoRegion['left'] &&
          centerX <= gabaritoRegion['right'];

      if (text.isNotEmpty && RegExp(r'^\d+$').hasMatch(text) && inRegion) {
        int num = int.tryParse(text) ?? -1;
        if (num > 0 && num <= 100) {
          questionNumbers.add({
            'number': num,
            'centerX': centerX,
            'centerY': centerY,
            'bottom': element['bottom'],
          });
        }
      }
    }

    print('Números de questões encontrados: ${questionNumbers.length}');

    // Para cada questão, analisar círculos abaixo
    for (var question in questionNumbers) {
      int questionNum = question['number'];
      double qX = question['centerX'];
      double qBottom = question['bottom'];

      print('\nAnalisando questão $questionNum');

      // Encontrar letras A-E abaixo do número
      List<Map<String, dynamic>> letters = [];

      for (var element in elements) {
        String text = element['text'].toUpperCase().trim();

        if (RegExp(r'^[A-E]$').hasMatch(text)) {
          double letterX = element['centerX'];
          double letterY = element['centerY'];

          double horizontalDist = (letterX - qX).abs();
          double verticalDist = letterY - qBottom;

          if (horizontalDist <= 100 && verticalDist >= -10 && verticalDist <= 80) {
            letters.add({
              'letter': text,
              'x': letterX,
              'y': letterY,
              'bottom': element['bottom'],
            });
          }
        }
      }

      if (letters.isEmpty) {
        print('  Nenhuma letra encontrada para questão $questionNum');
        continue;
      }

      letters.sort((a, b) => (a['x'] as double).compareTo(b['x'] as double));
      print('  Letras encontradas: ${letters.map((l) => l['letter']).join(', ')}');

      // Analisar cada círculo abaixo de cada letra
      String? markedLetter;
      double maxDarkness = 0.3; // Limiar mínimo de escuridão (30%)

      for (var letter in letters) {
        double circleX = letter['x'];
        double circleY = letter['bottom'] + 15; // 15px abaixo da letra

        // Analisar área do círculo (raio ~10px)
        double darkness = _analyzeCircleArea(grayImage, circleX, circleY, radius: 10);

        print('  ${letter['letter']}: escuridão = ${(darkness * 100).toStringAsFixed(1)}%');

        if (darkness > maxDarkness) {
          maxDarkness = darkness;
          markedLetter = letter['letter'];
        }
      }

      if (markedLetter != null) {
        respostas.add({
          'questao': questionNum.toString(),
          'resposta': markedLetter,
        });
        print('  ✓ Resposta detectada: $markedLetter (confiança: ${(maxDarkness * 100).toStringAsFixed(1)}%)');
      } else {
        print('  ✗ Nenhum círculo preenchido detectado');
      }
    }

    return respostas;
  }

  /// Analisa a escuridão de uma área circular
  double _analyzeCircleArea(img.Image image, double centerX, double centerY, {double radius = 10}) {
    int darkPixels = 0;
    int totalPixels = 0;

    int cx = centerX.toInt();
    int cy = centerY.toInt();
    int r = radius.toInt();

    // Verificar limites da imagem
    if (cx - r < 0 || cx + r >= image.width || cy - r < 0 || cy + r >= image.height) {
      return 0.0;
    }

    // Analisar pixels em um quadrado ao redor do círculo
    for (int y = cy - r; y <= cy + r; y++) {
      for (int x = cx - r; x <= cx + r; x++) {
        // Verificar se o pixel está dentro do círculo
        double dist = sqrt(pow(x - cx, 2) + pow(y - cy, 2));
        if (dist <= r) {
          totalPixels++;

          img.Pixel pixel = image.getPixel(x, y);
          int gray = pixel.r.toInt(); // Já está em escala de cinza

          // Considerar "escuro" se for menor que 128 (metade do range 0-255)
          if (gray < 128) {
            darkPixels++;
          }
        }
      }
    }

    return totalPixels > 0 ? darkPixels / totalPixels : 0.0;
  }

  void dispose() {
    _textRecognizer.close();
  }
}