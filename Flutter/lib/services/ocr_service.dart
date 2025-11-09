// lib/services/ocr_service.dart
import 'package:google_mlkit_text_recognition/google_mlkit_text_recognition.dart';
import 'package:image/image.dart' as img;
import 'dart:io';
import 'dart:math';

class OcrService {
  final TextRecognizer _textRecognizer = TextRecognizer();

  /// Processa o gabarito usando DETECÇÃO DE CÍRCULOS PREENCHIDOS
  Future<Map<String, dynamic>> processGabarito(String imagePath) async {
    print('=== INICIANDO PROCESSAMENTO (ESTRATÉGIA DE TABELA) ===');

    // 1. Carregar e processar a imagem
    final imageFile = File(imagePath);
    final bytes = await imageFile.readAsBytes();
    img.Image? image = img.decodeImage(bytes);

    if (image == null) {
      throw Exception('Não foi possível decodificar a imagem');
    }

    // 2. Converter para escala de cinza
    img.Image grayImage = img.grayscale(image);

    // 3. OCR para encontrar textos
    final inputImage = InputImage.fromFilePath(imagePath);
    final RecognizedText recognizedText = await _textRecognizer.processImage(inputImage);

    Map<String, dynamic> extractedData = {
      "respostas": [],
      "metadados": {
        "timestamp": DateTime.now().toIso8601String(),
        "metodo": "table_circle_detection",
        "confianca_media": 0.0
      }
    };

    // 4. Extrair elementos de texto
    List<Map<String, dynamic>> allElements = _extractTextElements(recognizedText);

    // 5. Encontrar região do gabarito (ainda útil)
    Map<String, dynamic>? gabaritoRegion = _findGabaritoRegion(allElements, image);

    if (gabaritoRegion == null) {
      print('⚠️ Região do gabarito não encontrada.');
      return extractedData;
    }
    print('✓ Região do gabarito encontrada');


    // 7. DETECÇÃO DE CÍRCULOS (NOVA LÓGICA DE TABELA)
    List<Map<String, String>> respostas = await _detectFilledCircles(
        grayImage,
        allElements,
        gabaritoRegion
    );

    extractedData["respostas"] = respostas;
    if (respostas.isNotEmpty) {
      extractedData["metadados"]["confianca_media"] = 0.85;
    }

    print('=== RESULTADO FINAL ===');
    print('Total de respostas: ${respostas.length}');
    print('Método: ${extractedData["metadados"]["metodo"]}');

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
          });
        }
      }
    }

    // Ordena de cima para baixo, depois da esquerda para a direita
    elements.sort((a, b) {
      int yCompare = (a['centerY'] as double).compareTo(b['centerY'] as double);
      if (yCompare.abs() > 10) return yCompare; // Se for claramente outra linha
      return (a['centerX'] as double).compareTo(b['centerX'] as double); // Mesma linha
    });

    return elements;
  }

  /// Encontra a região do gabarito na imagem (sem alteração)
  Map<String, dynamic>? _findGabaritoRegion(List<Map<String, dynamic>> elements, img.Image image) {
    for (var element in elements) {
      String text = element['text'].toUpperCase();
      if (text.contains('GABARITO')) {
        double top = element['bottom'] + 10;
        double left = 50.0;
        double right = image.width.toDouble() - 50;
        double bottom = min(top + 450, image.height.toDouble() - 50); // Área de busca

        return {
          'top': top,
          'left': left,
          'right': right,
          'bottom': bottom,
        };
      }
    }
    return null;
  }

  /// ATUALIZADO: DETECÇÃO DE CÍRCULOS (ESTRATÉGIA DE TABELA)
  Future<List<Map<String, String>>> _detectFilledCircles(
      img.Image grayImage,
      List<Map<String, dynamic>> elements,
      Map<String, dynamic> gabaritoRegion
      ) async {
    print('\n=== DETECTANDO CÍRCULOS (ESTRATÉGIA TABELA HORIZONTAL) ===');

    List<Map<String, String>> respostas = [];

    // 1. Encontrar números de questões E letras (A-E) na região
    List<Map<String, dynamic>> questionNumbers = [];
    List<Map<String, dynamic>> letters = [];

    for (var element in elements) {
      double centerY = element['centerY'];
      if (centerY < gabaritoRegion['top']! || centerY > gabaritoRegion['bottom']!) continue;

      String text = element['text'].toUpperCase().trim();

      // É um número de questão? (ex: "1", "2")
      if (RegExp(r'^\d+$').hasMatch(text) && (int.tryParse(text) ?? 0) <= 100) {
        questionNumbers.add({
          'number': int.parse(text),
          'centerX': element['centerX'],
          'centerY': element['centerY'],
          'bottom': element['bottom'],
        });
      }
      // É uma letra de alternativa? (ex: "A", "B")
      else if (RegExp(r'^[A-E]$').hasMatch(text)) {
        letters.add({
          'letter': text,
          'centerX': element['centerX'],
          'centerY': element['centerY'],
          'bottom': element['bottom'],
        });
      }
    }

    // Ordenar números por Y (linha a linha)
    questionNumbers.sort((a, b) => (a['centerY'] as double).compareTo(b['centerY'] as double));
    print('Números de questões encontrados: ${questionNumbers.length}');
    print('Letras de alternativa encontradas: ${letters.length}');

    // 3. Processar cada linha de questão
    for (var question in questionNumbers) {
      int questionNum = question['number'];
      double questionY = question['centerY'];
      double questionX = question['centerX'];

      print('\n📝 Questão $questionNum (Y: ${questionY.toStringAsFixed(1)})');

      // 4. Encontrar as letras (A-E) nesta mesma linha
      List<Map<String, dynamic>> lettersInRow = [];
      for (var letter in letters) {
        double letterY = letter['centerY'];
        double letterX = letter['centerX'];
        // Se a letra está na mesma linha (verticalmente próxima) E à direita do número
        if ((letterY - questionY).abs() < 25 && letterX > questionX) {
          lettersInRow.add(letter);
        }
      }

      if (lettersInRow.length < 3) { // Se não achar pelo menos 3 (A,B,C), ignora
        print('  ⚠️ Poucas letras (<3) encontradas na linha da questão.');
        continue;
      }

      // Ordenar letras da esquerda para direita
      lettersInRow.sort((a, b) => (a['centerX'] as double).compareTo(b['centerX'] as double));
      print('  Letras na linha: ${lettersInRow.map((l) => l['letter']).join(', ')}');

      // 5. Analisar círculos abaixo de cada letra
      String? markedLetter;
      double maxDarkness = 0.35; // Limiar: 35% de pixels escuros

      for (var letter in lettersInRow) {
        double circleX = letter['centerX'];
        // O PDF novo coloca o círculo 20-25px abaixo da letra
        double circleY = letter['bottom'] + 20; // 20px abaixo da letra
        double darkness = _analyzeCircleArea(grayImage, circleX, circleY, radius: 10); // Raio 10

        print('  ${letter['letter']}: ${(darkness * 100).toStringAsFixed(1)}% escuro');

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
        print('  ✓ Resposta: $markedLetter (${(maxDarkness * 100).toStringAsFixed(1)}%)');
      } else {
        print('  ✗ Nenhum círculo preenchido (máx: ${(maxDarkness * 100).toStringAsFixed(1)}%)');
      }
    }

    return respostas;
  }


  /// Analisa escuridão de uma área circular (sem alteração)
  double _analyzeCircleArea(img.Image image, double centerX, double centerY, {double radius = 10}) {
    int darkPixels = 0;
    int totalPixels = 0;

    int cx = centerX.toInt();
    int cy = centerY.toInt();
    int r = radius.toInt();

    // Verificar limites
    if (cx - r < 0 || cx + r >= image.width || cy - r < 0 || cy + r >= image.height) {
      return 0.0;
    }

    // Analisar pixels no círculo
    for (int y = cy - r; y <= cy + r; y++) {
      for (int x = cx - r; x <= cx + r; x++) {
        double dist = sqrt(pow(x - cx, 2) + pow(y - cy, 2));

        if (dist <= r) {
          totalPixels++;
          img.Pixel pixel = image.getPixel(x, y);
          int gray = pixel.r.toInt();
          if (gray < 130) { // Limiar de pixel escuro
            darkPixels++;
          }
        }
      }
    }
    return totalPixels > 0 ? darkPixels / totalPixels : 0.0;
  }

  // Método fallback (REMOVIDO, pois a nova estratégia é mais confiável)
  // List<Map<String, String>> _extractAnswersTextBased(...) { ... }

  void dispose() {
    _textRecognizer.close();
  }
}