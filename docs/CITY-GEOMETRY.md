> Registro da etapa 0.6.51, retida para a revisão da Nekomata. As correções descritas foram incorporadas à [entrega 0.6.52](NEKOMATA-CITY.md), que também substitui a Persona pela revisão 02 aprovada. A seção de auditoria ao final é histórica.

# 0.6.51 — Fechamentos da Gekkoukan e portas do dormitório

Esta versão corrige as falhas estruturais apontadas nas fotos da 0.6.50. O pátio permanece a céu aberto, com árvores de blocos nativos do Minecraft. As galerias dos pavimentos superiores agora têm paredes, janelas, pilares, vergas e cobertura contínua. A parede atrás da escada do hall e a fachada posterior também foram completadas, preservando os acessos ao pátio e ao ginásio e a escola como um único conjunto conectado.

No dormitório, paredes e rodapés deixaram de ocupar o mesmo plano dos batentes. As folhas dos quartos e da cobertura foram afastadas dos acabamentos; os lintéis foram ajustados sem deixar frestas. Na porta dupla da entrada, os ornamentos de latão passam a girar como peças completas com a dobradiça correspondente. A exportação anterior selecionava faces isoladas por posição e deixava partes dos arcos suspensas no vão aberto.

O adaptador das malhas para `BakedQuad` também mudou: o quadrilátero anterior repetia o último vértice, produzindo um triângulo degenerado. Agora os quatro vértices preservam exatamente a área e os atributos do triângulo original, dividindo-o em duas metades válidas. Isso remove uma fonte de tangentes inválidas nos renderizadores; compatibilidade visual com shader packs específicos ainda requer observação no jogo.

## Atualização de mundos

Planta da cidade na revisão **5**, canal da cidade no protocolo **6**. Cliente e servidor devem usar a mesma versão. As plantas e os estados das portas da revisão 4 foram preservados para a migração reconhecer tanto portas abertas quanto fechadas. Os IDs e os vértices antigos continuam disponíveis; blocos diferentes colocados pelo jogador seguem protegidos. O diário da escola durante a Dark Hour restaura a revisão anterior antes de atualizar a área.

Não há novos blocos ou itens nesta correção; os materiais existentes continuam nas abas criativas. Os computadores e a HUD não tiveram alterações de comportamento nesta versão.

## Reprodução e verificações

Exportação, nesta ordem:

```text
python tools/gekkoukan/export.py
python tools/city_polish/furniture.py
python tools/gekkoukan/furniture.py
python tools/gekkoukan/materials_maps.py
```

Verificações específicas:

```text
python tools/gekkoukan/audit_shell.py
python tools/gekkoukan/audit_jambs.py
python tools/gekkoukan/audit_entrance.py
python tools/gekkoukan/audit_paths.py
python tools/city_polish/audit_collision.py
python tools/gekkoukan/audit_legacy.py <jar-0.6.50>
gradlew.bat --offline --no-daemon cityGeometryTest cityFunctionTest cityPolishTest
```

As auditorias de fechamento, batentes e porta de entrada também aceitam o JAR anterior como argumento para demonstrar as falhas originais. `cityGeometryTest` verifica área e orientação das faces, variantes das portas, migração e restauração da escola. Os relatórios finais acompanham a entrega em `velvet-eclipse-0.6.51-validacao.json`.

As prévias usam os vértices e as texturas reais dos recursos exportados, com iluminação aproximada em renderizador offline. Não houve execução do Minecraft nem teste visual em shader packs.

Resultados da entrega: 5.606 amostras de fechamento e 3.545 dos batentes aprovadas; zero triângulos remanescentes no vão da entrada aberta (a 0.6.50 deixava 1.037 fragmentos exportados nessa região). Os 22 ambientes e seis percursos de escadas estão alcançáveis; dormitório e shopping passaram em 13.645 amostras de circulação e 19 destinos. Foram validadas 1.006.348 faces na conversão para quads, 4.741 condições de móveis/retornos e 43.406.514 condições de geometria e restauração. Os 96.308 IDs anteriores e 320.105.136 bytes de vértices permanecem idênticos.

## Nekomata — auditoria separada

A [auditoria da Nekomata](nekomata-audit/index.html) propõe a reconstrução do corpo, rosto, cabelo e cauda, seguindo o padrão de modelagem do projeto e as convenções do Cobblemon. Inclui o plano do **idle, ataque físico e ataque mágico**, com poses-chave e retorno ao idle.

As coreografias dos ataques são propostas para revisão, pois as referências consultadas não confirmaram uma sequência completa dos ataques de Reload. A nova Persona e suas animações ainda não foram modeladas ou integradas: os recursos atuais da Nekomata permanecem iguais. A auditoria inclui critérios para as próximas prévias e GIFs do modelo real.
