// lib/screens/camera_screen.dart
import 'package:camera/camera.dart';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'dart:io';
import 'package:hackathonflutter/services/ocr_service.dart';
import 'package:hackathonflutter/ui/widgets/msg_alerta.dart';
import 'package:hackathonflutter/screens/result_screen.dart'; // Importe ResultScreen
import 'package:provider/provider.dart';
import 'package:hackathonflutter/services/aluno_service.dart';
import 'package:hackathonflutter/services/avaliacao_service.dart';
import 'package:hackathonflutter/models/aluno.dart';
import 'package:hackathonflutter/models/prova.dart';

class CameraScreen extends StatefulWidget {
  const CameraScreen({super.key});

  @override
  State<CameraScreen> createState() => _CameraScreenState();
}

class _CameraScreenState extends State<CameraScreen> {
  CameraController? _controller;
  List<CameraDescription>? _cameras;
  XFile? _imageFile;
  late OcrService _ocrService;
  late AlunoService _alunoService;
  late AvaliacaoService _avaliacaoService;
  bool _isProcessing = false;

  @override
  void initState() {
    super.initState();
    _ocrService = Provider.of<OcrService>(context, listen: false);
    _alunoService = Provider.of<AlunoService>(context, listen: false);
    _avaliacaoService = Provider.of<AvaliacaoService>(context, listen: false);
    _initializeCamera();
  }

  Future<void> _initializeCamera() async {
    try {
      _cameras = await availableCameras();
      if (_cameras != null && _cameras!.isNotEmpty) {
        _controller = CameraController(_cameras![0], ResolutionPreset.high);
        await _controller!.initialize();
        if (!mounted) return;
        setState(() {});
      }
    } catch (e) {
      print("Erro ao inicializar a câmera: $e");
      if (mounted) {
        MsgAlerta.showError(context, 'Erro na Câmera', 'Não foi possível acessar a câmera: $e');
      }
    }
  }

  Future<void> _takePicture() async {
    if (!_controller!.value.isInitialized) {
      return;
    }
    setState(() {
      _isProcessing = true;
    });
    try {
      final image = await _controller!.takePicture();
      setState(() {
        _imageFile = image;
      });
      await _processImage(image.path); // Passa o caminho da imagem para processamento
    } catch (e) {
      print("Erro ao tirar foto: $e");
      if (mounted) {
        MsgAlerta.showError(context, 'Erro', 'Erro ao tirar a foto.');
      }
    } finally {
      setState(() {
        _isProcessing = false;
      });
    }
  }

  Future<void> _pickImage() async {
    setState(() {
      _isProcessing = true;
    });
    try {
      final ImagePicker picker = ImagePicker();
      final XFile? image = await picker.pickImage(source: ImageSource.gallery);
      if (image != null) {
        setState(() {
          _imageFile = image;
        });
        await _processImage(image.path); // Passa o caminho da imagem para processamento
      }
    } catch (e) {
      print("Erro ao selecionar imagem: $e");
      if (mounted) {
        MsgAlerta.showError(context, 'Erro', 'Erro ao selecionar imagem da galeria.');
      }
    } finally {
      setState(() {
        _isProcessing = false;
      });
    }
  }

  Future<void> _processImage(String imagePath) async {
    setState(() {
      _isProcessing = true;
    });
    try {
      final Map<String, dynamic> extractedData = await _ocrService.processGabarito(imagePath);

      if (mounted) {
        // Redireciona para a ResultScreen com os dados extraídos
        Navigator.pushReplacement(
          context,
          MaterialPageRoute(
            builder: (context) => ResultScreen(extractedData: extractedData),
          ),
        );
      }
    } catch (e) {
      print("Erro ao processar imagem: $e");
      if (mounted) {
        MsgAlerta.showError(context, 'Erro no OCR', 'Não foi possível processar o gabarito: $e');
      }
    } finally {
      setState(() {
        _isProcessing = false;
      });
    }
  }

  @override
  void dispose() {
    _controller?.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    if (_controller == null || !_controller!.value.isInitialized) {
      return const Center(child: CircularProgressIndicator());
    }
    return Scaffold(
      appBar: AppBar(title: const Text('Escanear Gabarito')),
      body: Stack(
        children: [
          Column(
            children: [
              Expanded(
                child: _imageFile == null
                    ? CameraPreview(_controller!)
                    : Image.file(File(_imageFile!.path)),
              ),
              Padding(
                padding: const EdgeInsets.all(8.0),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                  children: [
                    FloatingActionButton(
                      onPressed: _isProcessing ? null : _takePicture,
                      heroTag: 'takePicture',
                      child: const Icon(Icons.camera_alt),
                    ),
                    FloatingActionButton(
                      onPressed: _isProcessing ? null : _pickImage,
                      heroTag: 'pickImage',
                      child: const Icon(Icons.photo_library),
                    ),
                  ],
                ),
              ),
            ],
          ),
          if (_isProcessing)
            Container(
              color: Colors.black54,
              child: const Center(
                child: CircularProgressIndicator(),
              ),
            ),
        ],
      ),
    );
  }
}