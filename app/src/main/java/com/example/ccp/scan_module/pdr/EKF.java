package com.example.ccp.scan_module.pdr;

import static com.example.ccp.module.pos.AndGnssLModule.G_IO2;  // 초기위치 In Out 구분

public class EKF {

    public static double[][] P_pos= new double[4][4];
    public static double[][] P_pr= new double[4][4];
    public static double[][] P_post= new double[4][4];
    public static double[][] P_prii= new double[4][4];
    public static double[][] P_pri= new double[4][4];
    private static double[][] Qc= new double[4][4];;
    private static double[][] H_mat= new double[2][4];
    private static double[][] Ra= new double[2][2];
    private static double[][] Ka= new double[4][2];
    private static double[][] pri_= new double[2][2];
    private static double[][] pr_= new double[2][2];

    public static  double r;

    public static double[] pos_pP= new double[2];  // PDR position pos_pP[0] : lat(rad) , pos_pP[1] : lon(rad)
    public static double[] pos_wP= new double[2];  // WiFi position pos_wP[0] : lat(rad) , pos_wP[1] : lon(rad)


    public static void header_EKF(double lat){

        double p_lat=1.0/Glob_Pos.Rt;  //Lat 오차 공분산 3.0m
        double p_lon=1.0/(Glob_Pos.Rm*Math.cos(lat));  //Lon 오차 공분산 3.0m
        double d2r=Math.PI/180.0;
        double w_deg=10*d2r;  //(rad)
        double w_s=0.1;         //Stride  오차 공분산(0.001 m)  m
        double wq_deg=0.3 *d2r;  //Heading 오차 공분산(0.005 * d2r) rad
        //double r;
        if(G_IO2<0.6) {
            r = 0.10 / 6385493.0f;  // (WiFi Fingerprinting 오차 표준 편차)/Rt
        }
        else{
            r = 0.2 / 6385493.0f;  // (WiFi Fingerprinting 오차 표준 편차)/Rt
        }
        //  double r=0.000000078302;

        //오차 공분산 행렬  P_post
        P_post[0][0]=p_lat*p_lat;
        P_post[0][1]=0.0f;
        P_post[0][2]=0.0f;
        P_post[0][3]=0.0f;

        P_post[1][0]=0.0f;
        P_post[1][1]=p_lon*p_lon;
        P_post[1][2]=0.0f;
        P_post[1][3]=0.0;

        P_post[2][0]=0.0f;
        P_post[2][1]=0.0f;
        P_post[2][2]=w_s*w_s;
        P_post[2][3]=0.0f;

        P_post[3][0]=0.0f;
        P_post[3][1]=0.0f;
        P_post[3][2]=0.0f;
        P_post[3][3]=w_deg*w_deg;

        //Q 행렬
        Qc[0][0]=0.0f;
        Qc[0][1]=0.0f;
        Qc[0][2]=0.0f;
        Qc[0][3]=0.0f;

        Qc[1][0]=0.0f;
        Qc[1][1]=0.0f;
        Qc[1][2]=0.0f;
        Qc[1][3]=0.0f;

        Qc[2][0]=0.0f;
        Qc[2][1]=0.0f;
        Qc[2][2]=0.0f;
        Qc[2][3]=0.0f;

        Qc[3][0]=0.0f;
        Qc[3][1]=0.0f;
        Qc[3][2]=0.0f;
        Qc[3][3]=wq_deg*wq_deg;

        //R 행렬
        Ra[0][0]=r*r;
        Ra[0][1]=0.0f;
        Ra[1][0]=0.0f;
        Ra[1][1]=r*r;

        //H 행렬
        H_mat[0][0]=1.0f;
        H_mat[0][1]=0.0f;
        H_mat[0][2]=0.0f;
        H_mat[0][3]=0.0f;

        H_mat[1][0]=0.0f;
        H_mat[1][1]=1.0f;
        H_mat[1][2]=0.0f;
        H_mat[1][3]=0.0f;

    }

    public static void P_pri_Update(double Str,double Head){ // Str : Stride(m) , Head : Heading(rad)

        double[][] Fa = new double[4][4];

        //F Matrix
        Glob_Pos.radicurv(pos_pP[0]); //Rm,Rt 계산 , pos_pP[0] = PDR lat (rad)


        //F Matrix
        Fa[0][0]=1.0f;
        Fa[0][1]=0.0f;
        Fa[0][2]=Math.cos(Head)/Glob_Pos.Rm;                   //cos(Heading)/(Rm)
        Fa[0][3]=(-1.0f*Str*Math.sin(Head))/Glob_Pos.Rm;               //Stride * sin(Heading)/(Rm)

        Fa[1][0]=0.0;
        Fa[1][1]=1.0;
        Fa[1][2]=Math.sin(Head)/Glob_Pos.Rt;                   //sin(Heading)/(Rt)
        Fa[1][3]=(Str*Math.cos(Head))/(Glob_Pos.Rt*Math.cos(pos_pP[0]));   //Stride * cos(Heading)/(Rt*lat)

        Fa[2][0]=0.0f;
        Fa[2][1]=0.0f;
        Fa[2][2]=1.0f;
        Fa[2][3]=0.0f;

        Fa[3][0]=0.0f;
        Fa[3][1]=0.0f;
        Fa[3][2]=0.0f;
        Fa[3][3]=1.0f;

        /**
         *   F = | 1  0  cos(y)/Rm     -l * sin(y)/(Rm)         |
         *       | 0  1  sin(y)/Rt  l * cos(y)/(Rt * cos(Lat))  |
         *       | 0  0     1                 0                 |
         *       | 0  0     0                 1                 |
         y : Heading (rad) , l : Stride(m)
         */

//P_pri Update
        // P_prii = Fa * P_post * Fa' + Qc

        //P_pr=Fa * P_pos

        P_pr[0][0]= Fa[0][0]*P_post[0][0] + Fa[0][1]*P_post[1][0] + Fa[0][2]*P_post[2][0] + Fa[0][3]*P_post[3][0];
        P_pr[0][1]= Fa[0][0]*P_post[0][1] + Fa[0][1]*P_post[1][1] + Fa[0][2]*P_post[2][1] + Fa[0][3]*P_post[3][1];
        P_pr[0][2]= Fa[0][0]*P_post[0][2] + Fa[0][1]*P_post[1][2] + Fa[0][2]*P_post[2][2] + Fa[0][3]*P_post[3][2];
        P_pr[0][3]= Fa[0][0]*P_post[0][3] + Fa[0][1]*P_post[1][3] + Fa[0][2]*P_post[2][3] + Fa[0][3]*P_post[3][3];

        P_pr[1][0]= Fa[1][0]*P_post[0][0] + Fa[1][1]*P_post[1][0] + Fa[1][2]*P_post[2][0] + Fa[1][3]*P_post[3][0];
        P_pr[1][1]= Fa[1][0]*P_post[0][1] + Fa[1][1]*P_post[1][1] + Fa[1][2]*P_post[2][1] + Fa[1][3]*P_post[3][1];
        P_pr[1][2]= Fa[1][0]*P_post[0][2] + Fa[1][1]*P_post[1][2] + Fa[1][2]*P_post[2][2] + Fa[1][3]*P_post[3][2];
        P_pr[1][3]= Fa[1][0]*P_post[0][3] + Fa[1][1]*P_post[1][3] + Fa[1][2]*P_post[2][3] + Fa[1][3]*P_post[3][3];

        P_pr[2][0]= Fa[2][0]*P_post[0][0] + Fa[2][1]*P_post[1][0] + Fa[2][2]*P_post[2][0] + Fa[2][3]*P_post[3][0];
        P_pr[2][1]= Fa[2][0]*P_post[0][1] + Fa[2][1]*P_post[1][1] + Fa[2][2]*P_post[2][1] + Fa[2][3]*P_post[3][1];
        P_pr[2][2]= Fa[2][0]*P_post[0][2] + Fa[2][1]*P_post[1][2] + Fa[2][2]*P_post[2][2] + Fa[2][3]*P_post[3][2];
        P_pr[2][3]= Fa[2][0]*P_post[0][3] + Fa[2][1]*P_post[1][3] + Fa[2][2]*P_post[2][3] + Fa[2][3]*P_post[3][3];

        P_pr[3][0]= Fa[3][0]*P_post[0][0] + Fa[3][1]*P_post[1][0] + Fa[3][2]*P_post[2][0] + Fa[3][3]*P_post[3][0];
        P_pr[3][1]= Fa[3][0]*P_post[0][1] + Fa[3][1]*P_post[1][1] + Fa[3][2]*P_post[2][1] + Fa[3][3]*P_post[3][1];
        P_pr[3][2]= Fa[3][0]*P_post[0][2] + Fa[3][1]*P_post[1][2] + Fa[3][2]*P_post[2][2] + Fa[3][3]*P_post[3][2];
        P_pr[3][3]= Fa[3][0]*P_post[0][3] + Fa[3][1]*P_post[1][3] + Fa[3][2]*P_post[2][3] + Fa[3][3]*P_post[3][3];

        //P_pri=(Fa * P_pos)*Fa' + Qc

        P_prii[0][0]= P_pr[0][0]*Fa[0][0] + P_pr[0][1]*Fa[0][1] + P_pr[0][2]*Fa[0][2] + P_pr[0][3]*Fa[0][3] + Qc[0][0];
        P_prii[0][1]= P_pr[0][0]*Fa[1][0] + P_pr[0][1]*Fa[1][1] + P_pr[0][2]*Fa[1][2] + P_pr[0][3]*Fa[1][3] + Qc[0][1];
        P_prii[0][2]= P_pr[0][0]*Fa[2][0] + P_pr[0][1]*Fa[2][1] + P_pr[0][2]*Fa[2][2] + P_pr[0][3]*Fa[2][3] + Qc[0][2];
        P_prii[0][3]= P_pr[0][0]*Fa[3][0] + P_pr[0][1]*Fa[3][1] + P_pr[0][2]*Fa[3][2] + P_pr[0][3]*Fa[3][3] + Qc[0][3];

        P_prii[1][0]= P_pr[1][0]*Fa[0][0] + P_pr[1][1]*Fa[0][1] + P_pr[1][2]*Fa[0][2] + P_pr[1][3]*Fa[0][3] + Qc[1][0];
        P_prii[1][1]= P_pr[1][0]*Fa[1][0] + P_pr[1][1]*Fa[1][1] + P_pr[1][2]*Fa[1][2] + P_pr[1][3]*Fa[1][3] + Qc[1][1];
        P_prii[1][2]= P_pr[1][0]*Fa[2][0] + P_pr[1][1]*Fa[2][1] + P_pr[1][2]*Fa[2][2] + P_pr[1][3]*Fa[2][3] + Qc[1][2];
        P_prii[1][3]= P_pr[1][0]*Fa[3][0] + P_pr[1][1]*Fa[3][1] + P_pr[1][2]*Fa[3][2] + P_pr[1][3]*Fa[3][3] + Qc[1][3];

        P_prii[2][0]= P_pr[2][0]*Fa[0][0] + P_pr[2][1]*Fa[0][1] + P_pr[2][2]*Fa[0][2] + P_pr[2][3]*Fa[0][3] + Qc[2][0];
        P_prii[2][1]= P_pr[2][0]*Fa[1][0] + P_pr[2][1]*Fa[1][1] + P_pr[2][2]*Fa[1][2] + P_pr[2][3]*Fa[1][3] + Qc[2][1];
        P_prii[2][2]= P_pr[2][0]*Fa[2][0] + P_pr[2][1]*Fa[2][1] + P_pr[2][2]*Fa[2][2] + P_pr[2][3]*Fa[2][3] + Qc[2][2];
        P_prii[2][3]= P_pr[2][0]*Fa[3][0] + P_pr[2][1]*Fa[3][1] + P_pr[2][2]*Fa[3][2] + P_pr[2][3]*Fa[3][3] + Qc[2][3];

        P_prii[3][0]= P_pr[3][0]*Fa[0][0] + P_pr[3][1]*Fa[0][1] + P_pr[3][2]*Fa[0][2] + P_pr[3][3]*Fa[0][3] + Qc[3][0];
        P_prii[3][1]= P_pr[3][0]*Fa[1][0] + P_pr[3][1]*Fa[1][1] + P_pr[3][2]*Fa[1][2] + P_pr[3][3]*Fa[1][3] + Qc[3][1];
        P_prii[3][2]= P_pr[3][0]*Fa[2][0] + P_pr[3][1]*Fa[2][1] + P_pr[3][2]*Fa[2][2] + P_pr[3][3]*Fa[2][3] + Qc[3][2];
        P_prii[3][3]= P_pr[3][0]*Fa[3][0] + P_pr[3][1]*Fa[3][1] + P_pr[3][2]*Fa[3][2] + P_pr[3][3]*Fa[3][3] + Qc[3][3];

        // P_pri = (P_pri + P_pri')/2

        P_pri[0][0]= (P_prii[0][0]+P_prii[0][0])/2.0f;
        P_pri[0][1]= (P_prii[0][1]+P_prii[1][0])/2.0f;
        P_pri[0][2]= (P_prii[0][2]+P_prii[2][0])/2.0f;
        P_pri[0][3]= (P_prii[0][3]+P_prii[3][0])/2.0f;

        P_pri[1][0]= (P_prii[1][0]+P_prii[0][1])/2.0f;
        P_pri[1][1]= (P_prii[1][1]+P_prii[1][1])/2.0f;
        P_pri[1][2]= (P_prii[1][2]+P_prii[2][1])/2.0f;
        P_pri[1][3]= (P_prii[1][3]+P_prii[3][1])/2.0f;

        P_pri[2][0]= (P_prii[2][0]+P_prii[0][2])/2.0f;
        P_pri[2][1]= (P_prii[2][1]+P_prii[1][2])/2.0f;
        P_pri[2][2]= (P_prii[2][2]+P_prii[2][2])/2.0f;
        P_pri[2][3]= (P_prii[2][3]+P_prii[3][2])/2.0f;

        P_pri[3][0]= (P_prii[3][0]+P_prii[0][3])/2.0f;
        P_pri[3][1]= (P_prii[3][1]+P_prii[1][3])/2.0f;
        P_pri[3][2]= (P_prii[3][2]+P_prii[2][3])/2.0f;
        P_pri[3][3]= (P_prii[3][3]+P_prii[3][3])/2.0f;

    }


    public static double[] EKF_Update(double Str,double Head){

        double[] dz = new double[2];

        double[] dx_ = new double[4];
        //   if(G_IO==0) {
        //     r = 0.10 / 6385493.0;  // (WiFi Fingerprinting 오차 표준 편차)/Rt
        //   }
        //   else{
        //    r = 0.5 / 6385493.0;  // (WiFi Fingerprinting 오차 표준 편차)/Rt
        //   }
        //R 행렬
        Ra[0][0]=r*r;
        Ra[0][1]=0.0f;
        Ra[1][0]=0.0f;
        Ra[1][1]=r*r;

        dz[0]=pos_pP[0]-pos_wP[0]; //lat Error = PDR_lat (rad) - Measurement_GPS/WiFi Lat (rad)
        dz[1]=pos_pP[1]-pos_wP[1]; //lon Error = PDR_lon (rad) - Measurement_GPS/WiFi Lon (rad)
        //K gain Update
        //K = P_pri*H_mat' * inv(H_mat*P_pri*H_mat' + Ra);
        //  double[][] pri_= new double[2][2];

        //pri_=H_mat*P_pri*H_mat' + Ra
        pri_[0][0]=P_pri[0][0] + Ra[0][0];
        pri_[0][1]=P_pri[0][1] + Ra[0][1];

        pri_[1][0]=P_pri[1][0] + Ra[1][0];
        pri_[1][1]=P_pri[1][1] + Ra[1][1];

        /**
         H_mat*P_pri*H_mat' = | P_pri(0,0)  P_pri(0,1) |
         | P_pri(1,0)  P_pri(1,1) |
         */

        //pr_=inv(H_mat*P_pri*H_mat' + Ra)
        double detP=(pri_[0][0]*pri_[1][1])-(pri_[0][1]*pri_[1][0]); //(H_mat*P_pri*H_mat' + Ra) 의 행렬식 (ad-bc)

        pr_[0][0] = pri_[1][1]/detP;
        pr_[0][1] =-1.0f*(pri_[0][1]/detP);

        pr_[1][0] =-1.0f*(pri_[1][0]/detP);
        pr_[1][1] = pri_[0][0]/detP;

        //K =P_pri*H_mat'* pr_ = P_pri*H_mat'*inv(H_mat*P_pri*H_mat' + Ra);
        Ka[0][0] =P_pri[0][0]*pr_[0][0]+P_pri[0][1]*pr_[1][0];
        Ka[0][1] =P_pri[0][0]*pr_[0][1]+P_pri[0][1]*pr_[1][1];

        Ka[1][0] =P_pri[1][0]*pr_[0][0]+P_pri[1][1]*pr_[1][0];
        Ka[1][1] =P_pri[1][0]*pr_[0][1]+P_pri[1][1]*pr_[1][1];

        Ka[2][0] =P_pri[2][0]*pr_[0][0]+P_pri[2][1]*pr_[1][0];
        Ka[2][1] =P_pri[2][0]*pr_[0][1]+P_pri[2][1]*pr_[1][1];

        Ka[3][0] =P_pri[3][0]*pr_[0][0]+P_pri[3][1]*pr_[1][0];
        Ka[3][1] =P_pri[3][0]*pr_[0][1]+P_pri[3][1]*pr_[1][1];
        /**
         P_pri*H_mat' = | P_pri(0,0)  P_pri(0,1) |
         | P_pri(1,0)  P_pri(1,1) |
         | P_pri(2,0)  P_pri(2,1) |
         | P_pri(3,0)  P_pri(3,1) |
         */
        // dx_post = K * dz;
        dx_[0]=Ka[0][0]*dz[0]+Ka[0][1]*dz[1];
        dx_[1]=Ka[1][0]*dz[0]+Ka[1][1]*dz[1];
        dx_[2]=Ka[2][0]*dz[0]+Ka[2][1]*dz[1];
        dx_[3]=Ka[3][0]*dz[0]+Ka[3][1]*dz[1];


        //  P_post = (eye(4) - K*H_mat)*P_pri;
        //P_pos =(eye(4) - K*H_mat)
        P_pos[0][0]=1.0-Ka[0][0];
        P_pos[0][1]=0.0-Ka[0][1];
        P_pos[0][2]=0.0f;
        P_pos[0][3]=0.0f;

        P_pos[1][0]=0.0-Ka[1][0];
        P_pos[1][1]=1.0-Ka[1][1];
        P_pos[1][2]=0.0f;
        P_pos[1][3]=0.0f;

        P_pos[2][0]=0.0-Ka[2][0];
        P_pos[2][1]=0.0-Ka[2][1];
        P_pos[2][2]=1.0f;
        P_pos[2][3]=0.0f;

        P_pos[3][0]=0.0-Ka[3][0];
        P_pos[3][1]=0.0-Ka[3][1];
        P_pos[3][2]=0.0f;
        P_pos[3][3]=1.0f;
        /** K*H_mat = |  K(0,0)  K(0,1)  0  0  |
         |  K(1,0)  K(1,1)  0  0  |
         |  K(2,0)  K(2,1)  0  0  |
         |  K(3,0)  K(3,1)  0  0  |

         P_pos=eye(4)- K*H_mat = |  1-K(0,0)  0-K(0,1)  0  0  |
         |  0-K(1,0)  1-K(1,1)  0  0  |
         |  0-K(2,0)  0-K(2,1)  1  0  |
         |  0-K(3,0)  0-K(3,1)  0  1  |
         */
        //P_post=P_pos*P_pri = (eye(4) - K*H_mat)*P_pri
/**
 P_post[0][0]= P_pos[0][0]*P_pri[0][0] + P_pos[0][1]*P_pri[1][0] + P_pos[0][2]*P_pri[2][0] + P_pos[0][3]*P_pri[3][0];
 P_post[0][1]= P_pos[0][0]*P_pri[0][1] + P_pos[0][1]*P_pri[1][1] + P_pos[0][2]*P_pri[2][1] + P_pos[0][3]*P_pri[3][1];
 P_post[0][2]= P_pos[0][0]*P_pri[0][2] + P_pos[0][1]*P_pri[1][2] + P_pos[0][2]*P_pri[2][2] + P_pos[0][3]*P_pri[3][2];
 P_post[0][3]= P_pos[0][0]*P_pri[0][3] + P_pos[0][1]*P_pri[1][3] + P_pos[0][2]*P_pri[2][3] + P_pos[0][3]*P_pri[3][3];

 P_post[1][0]= P_pos[1][0]*P_pri[0][0] + P_pos[1][1]*P_pri[1][0] + P_pos[1][2]*P_pri[2][0] + P_pos[1][3]*P_pri[3][0];
 P_post[1][1]= P_pos[1][0]*P_pri[0][1] + P_pos[1][1]*P_pri[1][1] + P_pos[1][2]*P_pri[2][1] + P_pos[1][3]*P_pri[3][1];
 P_post[1][2]= P_pos[1][0]*P_pri[0][2] + P_pos[1][1]*P_pri[1][2] + P_pos[1][2]*P_pri[2][2] + P_pos[1][3]*P_pri[3][2];
 P_post[1][3]= P_pos[1][0]*P_pri[0][3] + P_pos[1][1]*P_pri[1][3] + P_pos[1][2]*P_pri[2][3] + P_pos[1][3]*P_pri[3][3];

 P_post[2][0]= P_pos[2][0]*P_pri[0][0] + P_pos[2][1]*P_pri[1][0] + P_pos[2][2]*P_pri[2][0] + P_pos[2][3]*P_pri[3][0];
 P_post[2][1]= P_pos[2][0]*P_pri[0][1] + P_pos[2][1]*P_pri[1][1] + P_pos[2][2]*P_pri[2][1] + P_pos[2][3]*P_pri[3][1];
 P_post[2][2]= P_pos[2][0]*P_pri[0][2] + P_pos[2][1]*P_pri[1][2] + P_pos[2][2]*P_pri[2][2] + P_pos[2][3]*P_pri[3][2];
 P_post[2][3]= P_pos[2][0]*P_pri[0][3] + P_pos[2][1]*P_pri[1][3] + P_pos[2][2]*P_pri[2][3] + P_pos[2][3]*P_pri[3][3];

 P_post[3][0]= P_pos[3][0]*P_pri[0][0] + P_pos[3][1]*P_pri[1][0] + P_pos[3][2]*P_pri[2][0] + P_pos[3][3]*P_pri[3][0];
 P_post[3][1]= P_pos[3][0]*P_pri[0][1] + P_pos[3][1]*P_pri[1][1] + P_pos[3][2]*P_pri[2][1] + P_pos[3][3]*P_pri[3][1];
 P_post[3][2]= P_pos[3][0]*P_pri[0][2] + P_pos[3][1]*P_pri[1][2] + P_pos[3][2]*P_pri[2][2] + P_pos[3][3]*P_pri[3][2];
 P_post[3][3]= P_pos[3][0]*P_pri[0][3] + P_pos[3][1]*P_pri[1][3] + P_pos[3][2]*P_pri[2][3] + P_pos[3][3]*P_pri[3][3];
 **/
        P_post[0][0]= P_pos[0][0]*P_pri[0][0] + P_pos[0][1]*P_pri[1][0] ;
        P_post[0][1]= P_pos[0][0]*P_pri[0][1] + P_pos[0][1]*P_pri[1][1] ;
        P_post[0][2]= P_pos[0][0]*P_pri[0][2] + P_pos[0][1]*P_pri[1][2] ;
        P_post[0][3]= P_pos[0][0]*P_pri[0][3] + P_pos[0][1]*P_pri[1][3] ;

        P_post[1][0]= P_pos[1][0]*P_pri[0][0] + P_pos[1][1]*P_pri[1][0] ;
        P_post[1][1]= P_pos[1][0]*P_pri[0][1] + P_pos[1][1]*P_pri[1][1] ;
        P_post[1][2]= P_pos[1][0]*P_pri[0][2] + P_pos[1][1]*P_pri[1][2] ;
        P_post[1][3]= P_pos[1][0]*P_pri[0][3] + P_pos[1][1]*P_pri[1][3] ;

        P_post[2][0]= P_pos[2][0]*P_pri[0][0] + P_pos[2][1]*P_pri[1][0] + P_pri[2][0] ;
        P_post[2][1]= P_pos[2][0]*P_pri[0][1] + P_pos[2][1]*P_pri[1][1] + P_pri[2][1] ;
        P_post[2][2]= P_pos[2][0]*P_pri[0][2] + P_pos[2][1]*P_pri[1][2] + P_pri[2][2] ;
        P_post[2][3]= P_pos[2][0]*P_pri[0][3] + P_pos[2][1]*P_pri[1][3] + P_pri[2][3] ;

        P_post[3][0]= P_pos[3][0]*P_pri[0][0] + P_pos[3][1]*P_pri[1][0] + P_pri[3][0];
        P_post[3][1]= P_pos[3][0]*P_pri[0][1] + P_pos[3][1]*P_pri[1][1] + P_pri[3][1];
        P_post[3][2]= P_pos[3][0]*P_pri[0][2] + P_pos[3][1]*P_pri[1][2] + P_pri[3][2];
        P_post[3][3]= P_pos[3][0]*P_pri[0][3] + P_pos[3][1]*P_pri[1][3] + P_pri[3][3];

        // EKF_ErrorComp
        pos_pP[0] = pos_pP[0]-dx_[0];  //pos_pP (PDR lat)
        pos_pP[1] = pos_pP[1]-dx_[1];  //pos_pP (PDR lon)
        Str=Str-dx_[2];
        Head=Head-dx_[3];
        //  Heading.yaw=Head-dx_[3];
        return dx_;
    }
}
