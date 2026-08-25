/** 登录相关类型（对齐 LoginController DTO/VO） */

export interface LoginDTO {
  /** 登录终端 0:web 1:app */
  terminal: number;
  /** 用户名 */
  userName: string;
  /** 用户密码 */
  password: string;
}

export interface LoginVO {
  accessToken: string;
  accessTokenExpiresIn?: number;
  refreshToken: string;
  refreshTokenExpiresIn?: number;
}

export interface RegisterDTO {
  userName: string;
  password: string;
  nickName?: string;
}

export interface ModifyPwdDTO {
  oldPassword: string;
  newPassword: string;
}
