// lib/models/prova.dart
import 'package:hackathonflutter/models/questao.dart';
import 'package:hackathonflutter/models/resposta.dart'; // Assumindo que você tem este modelo

class Prova {
  final int id;
  final String titulo;
  final String descricao;
  final String disciplinaNome;
  final List<Questao> questoes;
  final List<Resposta> respostasCorretas; // Para o gabarito

  Prova({
    required this.id,
    required this.titulo,
    required this.descricao,
    required this.disciplinaNome,
    this.questoes = const [], // Começa como lista vazia
    this.respostasCorretas = const [], // Começa como lista vazia
  });

  factory Prova.fromJson(Map<String, dynamic> json) {

    // --- ESTA É A CORREÇÃO ---
    // Usamos o operador '??' (null-coalescing) para fornecer um
    // valor padrão ("fallback") caso o JSON envie 'null'.

    String nomeDisciplina = 'Disciplina não informada';
    // Lógica para extrair o nome da disciplina, que pode vir aninhado
    if (json['disciplina'] != null && json['disciplina']['nome'] != null) {
      nomeDisciplina = json['disciplina']['nome'];
    }
    // O seu ProvaDTO pode já enviar o nome achatado
    else if (json['disciplinaNome'] != null) {
      nomeDisciplina = json['disciplinaNome'];
    }

    return Prova(
      id: json['id'],

      // AQUI ESTÁ A CORREÇÃO PRINCIPAL PARA O SEU ERRO:
      titulo: json['titulo'] ?? 'Título não informado',
      descricao: json['descricao'] ?? 'Sem descrição',
      disciplinaNome: nomeDisciplina,

      // Bónus: Tornar a lista de questões/respostas segura contra nulos
      // (Geralmente só vêm no endpoint /api/provas/{id})
      questoes: (json['questoes'] as List<dynamic>?)
          ?.map((q) => Questao.fromJson(q))
          .toList() ?? [], // Se 'questoes' for nulo, usa []

      respostasCorretas: (json['respostasCorretas'] as List<dynamic>?)
          ?.map((r) => Resposta.fromJson(r))
          .toList() ?? [], // Se 'respostasCorretas' for nulo, usa []
    );
  }
}