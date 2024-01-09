package com.example.sensorlog.scanner.sensor;


    public  class Mag_Heading {
        public static double a_roll,a_pitch;
        public static double Yaw_m;

        // calibration을 위한 지자기센서 Max,Min
        public static final double maxX=52.3125,maxY=52.0875,maxZ=52.8938;
        public static final double minX=-49.4063,minY=-49.6500,minZ=-53.2875;
        private static double[] Mag = new double[3];        //지자기 센서 값
        private static double[] Mag_c = new double[3];      //calibration된 지자기 값
        public static double r2d=180.0/Math.PI;
        public static double d2r=Math.PI/180.0;

        //가속도 ax,ay,az을 통한 Roll,Pitch
        public static void Accel_Attit(double ax,double ay, double az){

            a_roll = Math.atan2(ay,az);
            a_pitch = Math.atan2(ax,Math.sqrt(ay*ay+az*az));

        }

        public static double Out_Aroll(){

            return a_roll;
        }
        public static double Out_Apitch(){

            return a_pitch;
        }

        public static void In_mag(double mx, double my, double mz){
            Mag[0]=mx;

            Mag[1]=my;

            Mag[2]=mz;
        }

        public static double[] Out_mag(){
            return Mag;
        }
        public static double[] Mag_calibration(double mx, double my, double mz){

            Mag_c[0]=(mx-(maxX+minX)/2)*(2/(maxX-minX));
            Mag_c[1]=(my-(maxY+minY)/2)*(2/(maxY-minY));
            Mag_c[2]=(mz-(maxZ+minZ)/2)*(2/(maxZ-minZ));

            return Mag_c;
        }
        //지자기 센서를 활용한 Heading 계산
        public static double Mag_Attitude(double[] Mag){

            double den,nom;
            den = Mag[0]*Math.cos(a_pitch)+
                    Mag[1]*Math.sin(a_roll)*Math.sin(a_pitch) +
                    Mag[2]*Math.cos(a_roll)*Math.sin(a_pitch);


            nom = Mag[2]*Math.sin(a_roll)-Mag[1]*Math.cos(a_roll);



            Yaw_m = Math.atan2((-nom),den);

            return Yaw_m;
        }
    }

