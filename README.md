
## Escuela Colombiana de Ingeniería Julio Garavito
### Arquitecturas de Software – ARSW


#### Ejercicio – programación concurrente, condiciones de carrera y sincronización de hilos. EJERCICIO INDIVIDUAL O EN PAREJAS.

### Parte I – Antes de terminar la clase.

Control de hilos con wait/notify. Productor/consumidor.

1. Revise el funcionamiento del programa y ejecútelo. Mientras esto ocurren, ejecute jVisualVM y revise el consumo de CPU del proceso correspondiente. A qué se debe este consumo?, cual es la clase responsable?
	
![img.png](images%2Fimg.png)

- Muestra el consumo de CPU del proceso correspondiente al programa. El alto consumo de CPU se debe a que los hilos están en espera activa, es decir, están constantemente verificando si hay trabajo disponible sin liberar la CPU. La clase responsable de este comportamiento es la que maneja la cola de producción/consumo.

2. Haga los ajustes necesarios para que la solución use más eficientemente la CPU, teniendo en cuenta que -por ahora- la producción es lenta y el consumo es rápido. Verifique con JVisualVM que el consumo de CPU se reduzca.

![img_1.png](images%2Fimg_1.png)

- Muestra una reducción en el consumo de CPU después de realizar ajustes. Se implementaron mecanismos de sincronización como wait y notify para que los hilos liberen la CPU mientras esperan por trabajo, en lugar de estar en espera activa. Esto permite que la CPU se utilice de manera más eficiente.

3. Haga que ahora el productor produzca muy rápido, y el consumidor consuma lento. Teniendo en cuenta que el productor conoce un límite de Stock (cuantos elementos debería tener, a lo sumo en la cola), haga que dicho límite se respete. Revise el API de la colección usada como cola para ver cómo garantizar que dicho límite no se supere. Verifique que, al poner un límite pequeño para el 'stock', no haya consumo alto de CPU ni errores.

![img_2.png](images%2Fimg_2.png)

- Muestra el comportamiento del programa con un límite de stock pequeño. Se implementó un límite en la cola de producción para evitar que el productor añada más elementos de los permitidos. Esto se logró utilizando métodos de la API de la colección usada como cola para garantizar que el límite no se supere, evitando así un alto consumo de CPU y errores.

### Parte II. – Antes de terminar la clase.

Teniendo en cuenta los conceptos vistos de condición de carrera y sincronización, haga una nueva versión -más eficiente- del ejercicio anterior (el buscador de listas negras). En la versión actual, cada hilo se encarga de revisar el host en la totalidad del subconjunto de servidores que le corresponde, de manera que en conjunto se están explorando la totalidad de servidores. Teniendo esto en cuenta, haga que:

- La búsqueda distribuida se detenga (deje de buscar en las listas negras restantes) y retorne la respuesta apenas, en su conjunto, los hilos hayan detectado el número de ocurrencias requerido que determina si un host es confiable o no (_BLACK_LIST_ALARM_COUNT_).
- Lo anterior, garantizando que no se den condiciones de carrera.

	![img_3.png](images%2Fimg_3.png)

  - #### Explicación de la Implementación

	La nueva implementación del validador de listas negras distribuye la carga de trabajo entre múltiples hilos y permite la terminación temprana de la búsqueda cuando se alcanza un número específico de ocurrencias en las listas negras. Esto se logra mediante el uso de un estado compartido y mecanismos de sincronización para evitar condiciones de carrera.

	#### Componentes Principales

  1. **SharedState**: Clase que mantiene el estado compartido entre los hilos.
  2. **CheckSegment**: Clase que representa un hilo que busca en un segmento de servidores.
  3. **HostBlackListsValidator**: Clase principal que coordina la creación y ejecución de los hilos.

	#### Detalles de la Implementación

  1. **SharedState**:
      - Mantiene el número total de ocurrencias encontradas (`totalOccurrences`).
      - Almacena las listas negras en las que se encontró el host (`blacklists`).
      - Utiliza una bandera volátil (`stop`) para indicar a los hilos que deben detenerse.

  2. **CheckSegment**:
      - Cada hilo busca en un rango específico de servidores.
      - Si encuentra el host en una lista negra, incrementa el contador de ocurrencias en el estado compartido.
      - Si el número total de ocurrencias alcanza el umbral (`BLACK_LIST_ALARM_COUNT`), solicita la detención de todos los hilos.

  3. **HostBlackListsValidator**:
      - Crea y lanza los hilos (`CheckSegment`) para buscar en los servidores.
      - Espera a que todos los hilos terminen su ejecución.
      - Verifica si el número de ocurrencias encontradas es suficiente para considerar el host como no confiable.

	#### Sincronización y Terminación Temprana

  - **Sincronización**: Se utilizan bloques `synchronized` en la clase `SharedState` para asegurar que las operaciones de lectura y escritura en el estado compartido sean atómicas y seguras en un entorno multihilo.
  - **Terminación Temprana**: La bandera volátil `stop` permite que los hilos verifiquen periódicamente si deben detenerse, lo que permite una terminación temprana de la búsqueda cuando se alcanza el umbral de ocurrencias.

	Esta implementación mejora la eficiencia al evitar búsquedas innecesarias una vez que se ha determinado que el host es no confiable, y garantiza la seguridad en un entorno multihilo mediante el uso adecuado de mecanismos de sincronización.

### Parte III. – Avance para el martes, antes de clase.

Sincronización y Dead-Locks.

![](http://files.explosm.net/comics/Matt/Bummed-forever.png)

1. Revise el programa “highlander-simulator”, dispuesto en el paquete edu.eci.arsw.highlandersim. Este es un juego en el que:

	* Se tienen N jugadores inmortales.
	* Cada jugador conoce a los N-1 jugador restantes.
	* Cada jugador, permanentemente, ataca a algún otro inmortal. El que primero ataca le resta M puntos de vida a su contrincante, y aumenta en esta misma cantidad sus propios puntos de vida.
	* El juego podría nunca tener un único ganador. Lo más probable es que al final sólo queden dos, peleando indefinidamente quitando y sumando puntos de vida.

2. Revise el código e identifique cómo se implemento la funcionalidad antes indicada. Dada la intención del juego, un invariante debería ser que la sumatoria de los puntos de vida de todos los jugadores siempre sea el mismo(claro está, en un instante de tiempo en el que no esté en proceso una operación de incremento/reducción de tiempo). Para este caso, para N jugadores, cual debería ser este valor?.

	Para revisar cómo se implementó la funcionalidad del juego "highlander-simulator" y determinar el valor invariante de la sumatoria de los puntos de vida de todos los jugadores, debemos analizar el código de la clase `Immortal` y cómo se manejan los ataques entre los jugadores.

   1. **Clase `Immortal`**:
      - Cada `Immortal` tiene una lista de otros `Immortals` a los que puede atacar.
      - Los `Immortals` atacan a otros en un bucle infinito, restando puntos de vida a su oponente y sumándolos a los suyos.

   2. **Invariante de la Sumatoria de Puntos de Vida**:
      - La sumatoria de los puntos de vida de todos los jugadores debe ser constante.
      - Si cada jugador comienza con `DEFAULT_IMMORTAL_HEALTH` puntos de vida, y hay `N` jugadores, la sumatoria inicial de los puntos de vida es `N * DEFAULT_IMMORTAL_HEALTH`.

   ##### Ejemplo de Cálculo

      Si `N = 3` y `DEFAULT_IMMORTAL_HEALTH = 100`, entonces la sumatoria de los puntos de vida de todos los jugadores debería ser `3 * 100 = 300`.


3. Ejecute la aplicación y verifique cómo funcionan las opción ‘pause and check’. Se cumple el invariante?.

	- Al ejecutar la aplicación, la invariante no se esta calculando porque al tener 3 juegadores, se deberia mantener en 300. Pero se ve que los valores estan sobre el valor de 300.

	![img_4.png](images%2Fimg_4.png)

4. Una primera hipótesis para que se presente la condición de carrera para dicha función (pause and check), es que el programa consulta la lista cuyos valores va a imprimir, a la vez que otros hilos modifican sus valores. Para corregir esto, haga lo que sea necesario para que efectivamente, antes de imprimir los resultados actuales, se pausen todos los demás hilos. Adicionalmente, implemente la opción ‘resume’.

5. Verifique nuevamente el funcionamiento (haga clic muchas veces en el botón). Se cumple o no el invariante?.

	- Al implementar la opción 'pause and check' y 'resume', se logra pausar todos los hilos antes de imprimir los resultados actuales y reanudar su ejecución. Esto permite verificar si se cumple el invariante de la sumatoria de los puntos de vida de todos los jugadores.
    - Al verificar el funcionamiento de la aplicación, se observa que el invariante no se cumple, ya que la sumatoria de los puntos de vida de los jugadores no es constante. Esto se debe a que los hilos no están sincronizados adecuadamente, lo que puede causar condiciones de carrera y resultados inconsistentes.
6. Identifique posibles regiones críticas en lo que respecta a la pelea de los inmortales. Implemente una estrategia de bloqueo que evite las condiciones de carrera. Recuerde que si usted requiere usar dos o más ‘locks’ simultáneamente, puede usar bloques sincronizados anidados:

	```java
	synchronized(locka){
		synchronized(lockb){
			…
		}
	}
	```
	- Podemos encontrar que la región crítica en la pelea de los inmortales es cuando un inmortal ataca a otro, ya que en este momento se están modificando los puntos de vida de ambos jugadores. Para evitar condiciones de carrera, se puede utilizar un bloqueo en la operación de ataque para garantizar que no se realicen operaciones concurrentes en los puntos de vida de los jugadores.

7. Tras implementar su estrategia, ponga a correr su programa, y ponga atención a si éste se llega a detener. Si es así, use los programas jps y jstack para identificar por qué el programa se detuvo.
	
	- Al ejecutar el programa, se observa que se detiene después de un tiempo. Este es la imagen que aparece en ese momento: 
   
	![img_7.png](images%2Fimg_7.png)

	- Al utilizar los programas `jps` y `jstack`, se identifica que el programa se detiene debido a un `Deadlock`. Esto ocurre porque los hilos están esperando mutuamente a que se liberen los recursos que necesitan para continuar, lo que resulta en un bloqueo permanente.
   
	![img_6.png](images%2Fimg_6.png)


8. Plantee una estrategia para corregir el problema antes identificado (puede revisar de nuevo las páginas 206 y 207 de _Java Concurrency in Practice_).

	- Para corregir el problema se usará identityHashCode para evitar el deadlock. Se modificará el método `fight` de la clase `Immortal` para que los inmortales ataquen en orden ascendente según su identificador de hash. De esta manera, se evita que los hilos esperen mutuamente y se reduce la probabilidad de un deadlock.

9. Una vez corregido el problema, rectifique que el programa siga funcionando de manera consistente cuando se ejecutan 100, 1000 o 10000 inmortales. Si en estos casos grandes se empieza a incumplir de nuevo el invariante, debe analizar lo realizado en el paso 4.
	
	- Con 100 inmortales, el programa sigue funcionando de manera consistente y el invariante se mantiene.
	
   ![img_8.png](images%2Fimg_8.png)
	
	![img_9.png](images%2Fimg_9.png)
    - Con 1000 inmortales, el programa sigue funcionando de manera consistente y el invariante se mantiene.
   	
   ![img_10.png](images%2Fimg_10.png)
    
	![img_11.png](images%2Fimg_11.png)
    - Con 10000 inmortales, el programa sigue funcionando de manera consistente y el invariante se mantiene.
	
	![img_12.png](images%2Fimg_12.png)
	
	![img_13.png](images%2Fimg_13.png)

10. Un elemento molesto para la simulación es que en cierto punto de la misma hay pocos 'inmortales' vivos realizando peleas fallidas con 'inmortales' ya muertos. Es necesario ir suprimiendo los inmortales muertos de la simulación a medida que van muriendo. Para esto:
	* Analizando el esquema de funcionamiento de la simulación, esto podría crear una condición de carrera? Implemente la funcionalidad, ejecute la simulación y observe qué problema se presenta cuando hay muchos 'inmortales' en la misma. Escriba sus conclusiones al respecto en el archivo RESPUESTAS.txt.
	
    - La condición de carrera se presenta cuando varios hilos intentan acceder a la lista de inmortales para eliminar a los muertos al mismo tiempo. Esto puede causar inconsistencias en la lista y errores en la simulación. Para evitar este problema, se debe sincronizar el acceso a la lista de inmortales para garantizar que las operaciones de eliminación se realicen de manera segura y consistente.
    * Corrija el problema anterior __SIN hacer uso de sincronización__, pues volver secuencial el acceso a la lista compartida de inmortales haría extremadamente lenta la simulación.
	
	- Para corregir el problema sin hacer uso de sincronización, se puede utilizar una colección concurrente como `CopyOnWriteArrayList` para almacenar la lista de inmortales. Esta colección permite realizar operaciones de lectura y escritura concurrentes sin necesidad de sincronización, lo que mejora el rendimiento de la simulación.
11. Para finalizar, implemente la opción STOP.

<!--
### Criterios de evaluación

1. Parte I.
	* Funcional: La simulación de producción/consumidor se ejecuta eficientemente (sin esperas activas).

2. Parte II. (Retomando el laboratorio 1)
	* Se modificó el ejercicio anterior para que los hilos llevaran conjuntamente (compartido) el número de ocurrencias encontradas, y se finalizaran y retornaran el valor en cuanto dicho número de ocurrencias fuera el esperado.
	* Se garantiza que no se den condiciones de carrera modificando el acceso concurrente al valor compartido (número de ocurrencias).


2. Parte III.
	* Diseño:
		- Coordinación de hilos:
			* Para pausar la pelea, se debe lograr que el hilo principal induzca a los otros a que se suspendan a sí mismos. Se debe también tener en cuenta que sólo se debe mostrar la sumatoria de los puntos de vida cuando se asegure que todos los hilos han sido suspendidos.
			* Si para lo anterior se recorre a todo el conjunto de hilos para ver su estado, se evalúa como R, por ser muy ineficiente.
			* Si para lo anterior los hilos manipulan un contador concurrentemente, pero lo hacen sin tener en cuenta que el incremento de un contador no es una operación atómica -es decir, que puede causar una condición de carrera- , se evalúa como R. En este caso se debería sincronizar el acceso, o usar tipos atómicos como AtomicInteger).

		- Consistencia ante la concurrencia
			* Para garantizar la consistencia en la pelea entre dos inmortales, se debe sincronizar el acceso a cualquier otra pelea que involucre a uno, al otro, o a los dos simultáneamente:
			* En los bloques anidados de sincronización requeridos para lo anterior, se debe garantizar que si los mismos locks son usados en dos peleas simultánemante, éstos será usados en el mismo orden para evitar deadlocks.
			* En caso de sincronizar el acceso a la pelea con un LOCK común, se evaluará como M, pues esto hace secuencial todas las peleas.
			* La lista de inmortales debe reducirse en la medida que éstos mueran, pero esta operación debe realizarse SIN sincronización, sino haciendo uso de una colección concurrente (no bloqueante).

	

	* Funcionalidad:
		* Se cumple con el invariante al usar la aplicación con 10, 100 o 1000 hilos.
		* La aplicación puede reanudar y finalizar(stop) su ejecución.
		
		-->

<a rel="license" href="http://creativecommons.org/licenses/by-nc/4.0/"><img alt="Creative Commons License" style="border-width:0" src="https://i.creativecommons.org/l/by-nc/4.0/88x31.png" /></a><br />Este contenido hace parte del curso Arquitecturas de Software del programa de Ingeniería de Sistemas de la Escuela Colombiana de Ingeniería, y está licenciado como <a rel="license" href="http://creativecommons.org/licenses/by-nc/4.0/">Creative Commons Attribution-NonCommercial 4.0 International License</a>.
