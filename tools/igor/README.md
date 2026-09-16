# Igor — estudo voxel 02

Modelo separado para avaliação; não integrado ao mod. O usuário aprovou o aspecto geral e pediu apenas olhos menores; esta revisão reduz sua largura e altura de 3,5 para 2,75 unidades (21,4%).

Pose sentada com mãos cruzadas, terno escuro, camisa e punhos brancos, luvas com dedos modelados, lenço de bolso, cabelo branco lateral, sobrancelhas arqueadas, sorriso e nariz longo angular. O nariz usa quatro planos triangulares por recorte de textura, mantendo Box UV e tamanhos inteiros. O projeto editável é `draft/igor.bbmodel`, com textura incorporada. `igor.geo.json`, `igor.png` e `igor.animation.json` também estão separados. Não copiar o JSON diretamente para o mod: a conversão de eixos e escala do GeckoLib será feita apenas numa futura integração autorizada.

Autoria: `python build_model.py`. Prévia: `python render_model.py`. Python com Pillow e NumPy. O renderizador usa as mesmas coordenadas e o mesmo atlas do modelo; não usa imagens sintetizadas. Tipografia da prancha usa Segoe UI do Windows.

Padrão aplicado: entidades Bedrock, Box UV, tamanhos inteiros, frente North, grupos e pivôs, grade uniforme. Densidade de autoria 4; o modelo ampliado conserva 1 texel por unidade exportada e poderá receber escala global na integração. A animação é um idle discreto de 4,8 s, com os pés e o assento fixos.

Referência técnica: [Creating A Model — Cobblemon Wiki](https://wiki.cobblemon.com/index.php/Tutorials/Creating_A_Model), consultada em 13/09/2026. Referência visual: imagem de Igor fornecida pelo usuário. Padrão local: `../izanagi/MODELING.md`.
