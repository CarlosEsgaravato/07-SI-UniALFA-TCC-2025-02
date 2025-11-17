import 'package:flutter/material.dart';
import 'package:hackathonflutter/ui/widgets/msg_alerta.dart';
import 'package:hackathonflutter/ui/widgets/circulo_espera.dart';
import 'package:provider/provider.dart';

class ResultScreen extends StatefulWidget {
  final Map<String, dynamic> extractedData;

  const ResultScreen({super.key, required this.extractedData});

  @override
  State<ResultScreen> createState() => _ResultScreenState();
}

class _ResultScreenState extends State<ResultScreen> {
  late Map<String, TextEditingController> _responseControllers;
  late List<String> _questionOrder;
  bool _isLoading = false;

  @override
  void initState() {
    super.initState();
    _responseControllers = {};
    _questionOrder = [];
    final List<Map<String, String>> respostas = List<Map<String, String>>.from(widget.extractedData['respostas'] ?? []);

    for (var resposta in respostas) {
      final String questaoNum = resposta['questao'] ?? '';
      final String questaoResp = resposta['resposta'] ?? '';
      if (questaoNum.isNotEmpty) {
        _questionOrder.add(questaoNum);
        _responseControllers[questaoNum] = TextEditingController(text: questaoResp);
      }
    }
  }

  @override
  void dispose() {
    _responseControllers.forEach((_, controller) => controller.dispose());
    super.dispose();
  }
  List<Map<String, String>> _buildUpdatedResponses() {
    final List<Map<String, String>> updatedResponses = [];
    for (var questaoNum in _questionOrder) {
      final controller = _responseControllers[questaoNum];
      if (controller != null) {
        updatedResponses.add({
          'questao': questaoNum,
          'resposta': controller.text.toUpperCase(),
        });
      }
    }
    return updatedResponses;
  }

  void _confirmarEDevolver() {

    final respostasCorrigidas = _buildUpdatedResponses();

    Navigator.pop(context, {
      'respostas': respostasCorrigidas,
    });
  }


  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Confirmar Leitura OCR'),
        automaticallyImplyLeading: false,
      ),
      body: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [

          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 16.0), // Adicionado padding vertical
            child: Text('Respostas Lidas (${_questionOrder.length}):', style: Theme.of(context).textTheme.titleMedium),
          ),
          const SizedBox(height: 10),
          Expanded(
            child: _isLoading
                ? const Center(child: CirculoEspera())
                : _questionOrder.isEmpty
                ? const Center(
              child: Padding(
                padding: EdgeInsets.all(16.0),
                child: Text(
                  'Nenhuma resposta foi lida pelo OCR. Verifique a iluminação e a qualidade da foto.',
                  textAlign: TextAlign.center,
                  style: TextStyle(fontSize: 16),
                ),
              ),
            )
                : ListView.builder(
              padding: const EdgeInsets.symmetric(horizontal: 16.0),
              itemCount: _questionOrder.length,
              itemBuilder: (context, index) {
                final questaoNum = _questionOrder[index];
                final controller = _responseControllers[questaoNum];

                if (controller == null) return const SizedBox.shrink();

                return Card(
                  margin: const EdgeInsets.symmetric(vertical: 4),
                  child: Padding(
                    padding: const EdgeInsets.all(12.0),
                    child: Row(
                      children: [
                        Text(
                          'Questão ${questaoNum.padLeft(2, '0')}:',
                          style: const TextStyle(fontWeight: FontWeight.bold),
                        ),
                        const SizedBox(width: 16),
                        Expanded(
                          child: TextField(
                            controller: controller, // Usa o controller correto
                            decoration: const InputDecoration(
                              border: OutlineInputBorder(),
                              contentPadding: EdgeInsets.symmetric(horizontal: 10, vertical: 8),
                              counterText: '',
                            ),
                            textAlign: TextAlign.center,
                            maxLength: 1,
                            textCapitalization: TextCapitalization.characters,
                          ),
                        ),
                      ],
                    ),
                  ),
                );
              },
            ),
          ),
          Container(
            padding: const EdgeInsets.all(16.0),
            decoration: BoxDecoration(
              color: Theme.of(context).scaffoldBackgroundColor,
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withOpacity(0.1),
                  blurRadius: 4,
                  offset: const Offset(0, -2),
                ),
              ],
            ),
            child: _isLoading
                ? const Center(child: CirculoEspera())
                : Column(
              children: [
                ElevatedButton.icon(
                  onPressed: _isLoading ? null : _confirmarEDevolver,
                  icon: const Icon(Icons.check_circle_outline),
                  label: const Text('Confirmar Dados e Voltar'),
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Colors.blue,
                    foregroundColor: Colors.white,
                    minimumSize: const Size(double.infinity, 50),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}