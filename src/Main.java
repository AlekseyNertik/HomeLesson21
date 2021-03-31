import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

class MainClass {
        public static final int CARS_COUNT = 4;
    public static final int HALF_CARS_COUNT = CARS_COUNT/2;
        public static void main(String[] args) {
            System.out.println("ВАЖНОЕ ОБЪЯВЛЕНИЕ >>> Подготовка!!!");
            final int THREADS_COUNT = 4; //количество контролируемых потоков на ожидание 4
            final CountDownLatch cdl = new CountDownLatch(THREADS_COUNT); //выделяю ресурс на ожидание
            CyclicBarrier cb = new CyclicBarrier(5); //количество контролируемых потоков на ожидание 4

            Race race = new Race(new Road(60), new Tunnel(), new Road(40));
            Car[] cars = new Car[CARS_COUNT];
            for (int i = 0; i < cars.length; i++) {
                cars[i] = new Car(race, 20 + (int) (Math.random() * 10), cb, cdl);
            }
            for (int i = 0; i < cars.length; i++) {
                new Thread(cars[i]).start();
            }
            try {
                cb.await();
                cb.await();
                System.out.println("ВАЖНОЕ ОБЪЯВЛЕНИЕ >>> Гонка началась!!!");
                cb.await();
            } catch (Exception e) {
                e.printStackTrace();
            }
           System.out.println("ВАЖНОЕ ОБЪЯВЛЕНИЕ >>> Гонка закончилась!!!");

        }

}
    class Car implements Runnable {
        private static int CARS_COUNT;
        private CyclicBarrier cbc;
        private CountDownLatch cdl;
        private static boolean winFound;
        static {
            CARS_COUNT = 0;
        }
        private Race race;
        private int speed;
        private String name;

        public String getName() {
            return name;
        }
        public int getSpeed() {
            return speed;
        }

        public Car(Race race, int speed, CyclicBarrier cbc, CountDownLatch cdl) {
            this.race = race;
            this.speed = speed;
            CARS_COUNT++;
            this.name = "Участник #" + CARS_COUNT;
            this.cbc = cbc;
            this.cdl = cdl;
        }
        @Override
        public void run() {
            try {
                System.out.println(this.name + " готовится");
                Thread.sleep(500 + (int)(Math.random() * 800));
                System.out.println(this.name + " готов,  скорость "+ this.speed); // где-то тут цикл
                cbc.await();
                cbc.await();
                for (int i = 0; i < race.getStages().size(); i++) {
                    race.getStages().get(i).go(this);
                }
            checkWin(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                cbc.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
        }

        private static synchronized void checkWin(Car c) {
            if (!winFound) {
                System.out.println(c.name+" - ====== WINNER!!! ====");
                winFound=true;
            }
        }
    }
    abstract class Stage {
        protected int length;
        protected String description;
        public String getDescription() {
            return description;
        }
        public abstract void go(Car c);
    }
    class Road extends Stage {
        public Road(int length) {
            this.length = length;
            this.description = "Дорога " + length + " метров";
        }
        @Override
        public void go(Car c) {
            try {
                System.out.println(c.getName() + " начал этап: " + description);
                Thread.sleep(length / c.getSpeed() * 1000);
                System.out.println(c.getName() + " закончил этап: " + description);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    class Tunnel extends Stage {
        private static final int HALF_CARS_COUNT = 2;
        Semaphore sem = new Semaphore(HALF_CARS_COUNT);
        public Tunnel() {
            this.length = 80;
            this.description = "Тоннель " + length + " метров";
        }
        @Override
        public void go(Car c) {
            try {  // тут сужение
                try {
                    System.out.println(c.getName() + " готовится к этапу(ждет): " + description);
                    sem.acquire();
                    System.out.println(c.getName() + " начал этап: " + description);
                    Thread.sleep(length / c.getSpeed() * 500);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    System.out.println(c.getName() + " закончил этап: " + description);
                    sem.release();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    class Race {
        private ArrayList<Stage> stages;
        public ArrayList<Stage> getStages() { return stages; }
        public Race(Stage... stages) {
            this.stages = new ArrayList<>(Arrays.asList(stages));
        }
    }



