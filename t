 .vscode/settings.json                              |   3 [32m++[m[31m-[m
 pom.xml                                            |   2 [32m++[m
 src/main/java/com/ferreteria/v1/models/Compra.java |   5 [32m++++[m[31m-[m
 src/main/java/com/ferreteria/v1/models/Venta.java  |   4 [32m+++[m[31m-[m
 .../v1/repositories/CompraRepository.java          |  19 [32m++++++++++++++++++[m
 .../v1/repositories/ProductoRepository.java        |   8 [32m++++++++[m
 .../v1/repositories/VentaRepository.java           |  10 [32m++++++++++[m
 .../com/ferreteria/v1/services/CompraService.java  |  22 [32m++++++++++++++++++[m[31m---[m
 .../java/com/ferreteria/v1/services/IaService.java |  22 [32m++++++++++++++++++[m[31m---[m
 .../com/ferreteria/v1/services/ReporteService.java |  19 [32m+++++++++[m[31m---------[m
 .../com/ferreteria/v1/services/VentaService.java   |  21 [32m+++++++++++++++++[m[31m---[m
 src/main/resources/application.properties          |   8 [32m++++++[m[31m--[m
 .../ferreteria/v1/services/CompraServiceTest.java  |   6 [32m+++[m[31m---[m
 .../ferreteria/v1/services/VentaServiceTest.java   |   7 [32m+++++[m[31m--[m
 target/classes/application.properties              |   8 [32m++++++[m[31m--[m
 .../ferreteria/v1/credito/CuentaPorCobrar.class    | Bin [31m1559[m -> [32m0[m bytes
 .../com/ferreteria/v1/credito/EnviadorCorreo.class | Bin [31m278[m -> [32m0[m bytes
 .../com/ferreteria/v1/credito/EstadoCuenta.class   | Bin [31m1154[m -> [32m0[m bytes
 .../com/ferreteria/v1/credito/EstadoMora.class     | Bin [31m1190[m -> [32m0[m bytes
 .../ferreteria/v1/credito/MoraClienteService.class | Bin [31m1635[m -> [32m0[m bytes
 .../ferreteria/v1/credito/NotificadorPago.class    | Bin [31m2402[m -> [32m0[m bytes
 .../ferreteria/v1/credito/PoliticaCredito.class    | Bin [31m994[m -> [32m0[m bytes
 .../com/ferreteria/v1/credito/ResultadoEnvio.class | Bin [31m1222[m -> [32m0[m bytes
 .../com/ferreteria/v1/models/Compra$Estado.class   | Bin [31m1296[m -> [32m1296[m bytes
 .../com/ferreteria/v1/models/Compra$TipoPago.class | Bin [31m1314[m -> [32m1314[m bytes
 .../classes/com/ferreteria/v1/models/Compra.class  | Bin [31m5672[m -> [32m5793[m bytes
 .../ferreteria/v1/models/Venta$EstadoVenta.class   | Bin [31m1273[m -> [32m1273[m bytes
 .../v1/models/Venta$TipoComprobante.class          | Bin [31m1297[m -> [32m1297[m bytes
 .../com/ferreteria/v1/models/Venta$TipoPago.class  | Bin [31m1344[m -> [32m1344[m bytes
 .../classes/com/ferreteria/v1/models/Venta.class   | Bin [31m5227[m -> [32m5347[m bytes
 .../v1/repositories/CompraRepository.class         | Bin [31m441[m -> [32m1476[m bytes
 .../v1/repositories/ProductoRepository.class       | Bin [31m1145[m -> [32m1673[m bytes
 .../v1/repositories/VentaRepository.class          | Bin [31m1368[m -> [32m1945[m bytes
 .../com/ferreteria/v1/services/CompraService.class | Bin [31m4588[m -> [32m6199[m bytes
 .../com/ferreteria/v1/services/IaService.class     | Bin [31m14015[m -> [32m15096[m bytes
 .../ferreteria/v1/services/ReporteService.class    | Bin [31m7087[m -> [32m7363[m bytes
 .../com/ferreteria/v1/services/VentaService.class  | Bin [31m9883[m -> [32m10708[m bytes
 .../v1/credito/CuentaPorCobrarTest.class           | Bin [31m4522[m -> [32m0[m bytes
 .../v1/credito/MoraClienteServiceTest.class        | Bin [31m2304[m -> [32m0[m bytes
 .../v1/credito/NotificadorPagoTest.class           | Bin [31m3861[m -> [32m0[m bytes
 .../v1/credito/PoliticaCreditoTest.class           | Bin [31m3047[m -> [32m0[m bytes
 .../ferreteria/v1/services/CompraServiceTest.class | Bin [31m10887[m -> [32m10969[m bytes
 .../ferreteria/v1/services/VentaServiceTest.class  | Bin [31m15523[m -> [32m15875[m bytes
 43 files changed, 133 insertions(+), 31 deletions(-)
