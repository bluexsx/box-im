/** 文件上传相关类型（对齐 FileController） */

export interface UploadImageVO {
  /** 原图 */
  originUrl?: string;
  /** 缩略图 */
  thumbUrl?: string;
  /** 图片宽度 */
  width?: number;
  /** 图片高度 */
  height?: number;
}
