import os
import shutil
from PIL import Image
import argparse
from pathlib import Path

def ensure_unique_filename(filepath):
    """
    确保文件名唯一，如果文件已存在则添加数字后缀
    """
    if not os.path.exists(filepath):
        return filepath

    directory = os.path.dirname(filepath)
    filename = os.path.basename(filepath)
    name, ext = os.path.splitext(filename)

    counter = 1
    while True:
        new_filename = f"{name}_{counter}{ext}"
        new_filepath = os.path.join(directory, new_filename)
        if not os.path.exists(new_filepath):
            return new_filepath
        counter += 1

def copy_file_with_unique_name(src_path, dst_path):
    """
    复制文件，如果目标已存在则使用唯一文件名
    """
    # 检查目标文件是否已存在
    if os.path.exists(dst_path):
        dst_path = Path(ensure_unique_filename(str(dst_path)))

    # 复制文件
    shutil.copy2(src_path, dst_path)
    return dst_path

def process_images(input_folder, output_folder, max_width=150):
    """
    处理指定文件夹下的所有文件：
    - 图片文件（宽度超过max_width）: 缩放后保存
    - 其他文件（非图片或宽度不足的图片）: 直接复制

    Args:
        input_folder: 输入文件夹路径
        output_folder: 输出文件夹路径
        max_width: 目标宽度（像素），默认150
    """

    # 支持的图片格式
    supported_formats = {'.jpg', '.jpeg', '.png', '.bmp', '.tiff', '.webp'}

    # 创建输出文件夹（如果不存在）
    output_path = Path(output_folder)
    output_path.mkdir(parents=True, exist_ok=True)

    # 统计信息
    processed_count = 0  # 已处理的图片（缩放）
    copied_count = 0      # 直接复制的文件
    skipped_count = 0     # 跳过的文件（宽度不足的图片）
    error_count = 0       # 错误数
    already_exists_count = 0  # 文件名冲突数

    # 遍历输入文件夹
    input_path = Path(input_folder)
    for file_path in input_path.rglob('*'):
        # 检查是否为文件
        if not file_path.is_file():
            continue

        try:
            # 生成输出文件路径（保持原文件名）
            relative_path = file_path.relative_to(input_path)
            output_filepath = output_path / relative_path

            # 确保输出目录存在
            output_filepath.parent.mkdir(parents=True, exist_ok=True)

            # 检查文件扩展名
            file_ext = file_path.suffix.lower()

            # 如果是支持的图片格式
            if file_ext in supported_formats:
                try:
                    # 打开图片获取尺寸
                    with Image.open(file_path) as img:
                        original_width, original_height = img.size

                        # 检查宽度是否超过阈值
                        if original_width > max_width:
                            # 计算新尺寸（保持长宽比）
                            new_width = max_width
                            new_height = int((max_width / original_width) * original_height)

                            # 缩放图片
                            resized_img = img.resize((new_width, new_height), Image.Resampling.LANCZOS)

                            # 检查文件是否已存在
                            if output_filepath.exists():
                                already_exists_count += 1
                                # 生成唯一文件名
                                output_filepath = Path(ensure_unique_filename(str(output_filepath)))

                            # 保存图片
                            resized_img.save(output_filepath, optimize=True, quality=85)

                            print(f"[缩放] {file_path.name} -> {output_filepath.name} "
                                  f"({original_width}x{original_height} -> {new_width}x{new_height})")
                            processed_count += 1
                        else:
                            # 宽度不足，直接复制原图
                            final_path = copy_file_with_unique_name(file_path, output_filepath)
                            print(f"[复制-宽度不足] {file_path.name} -> {final_path.name} "
                                  f"(宽度{original_width}像素，未超过{max_width}像素)")
                            skipped_count += 1

                except Exception as e:
                    # 如果无法打开图片（可能图片损坏），也直接复制
                    print(f"[警告] 无法打开图片 {file_path.name}，将直接复制: {str(e)}")
                    final_path = copy_file_with_unique_name(file_path, output_filepath)
                    copied_count += 1

            else:
                # 非图片格式文件，直接复制
                final_path = copy_file_with_unique_name(file_path, output_filepath)
                print(f"[复制-非图片] {file_path.name} -> {final_path.name}")
                copied_count += 1

        except Exception as e:
            print(f"[错误] 处理文件 {file_path.name} 时发生错误: {str(e)}")
            error_count += 1

    # 打印统计信息
    print("\n" + "="*60)
    print(f"处理完成!")
    print(f"输入文件夹: {input_folder}")
    print(f"输出文件夹: {output_folder}")
    print(f"目标宽度: {max_width} 像素")
    print("-" * 60)
    print(f"总处理数: {processed_count + skipped_count + copied_count} 个")
    print(f"缩放数: {processed_count} 张")
    print(f"复制数: {skipped_count + copied_count} 个")
    print(f"  ├─ 宽度不足的图片: {skipped_count} 张")
    print(f"  └─ 非图片文件: {copied_count} 个")
    print(f"冲突数: {already_exists_count} 次")
    print(f"错误数: {error_count} 个")
    print("="*60)

def main():
    # 创建命令行参数解析器
    parser = argparse.ArgumentParser(description='批量处理图片：缩放宽度超过阈值的图片，其他文件直接复制')
    parser.add_argument('input_folder', help='输入文件夹路径')
    parser.add_argument('output_folder', help='输出文件夹路径')
    parser.add_argument('--width', type=int, default=150,
                       help='目标宽度（像素），默认150')

    args = parser.parse_args()

    # 检查输入文件夹是否存在
    if not os.path.exists(args.input_folder):
        print(f"错误: 输入文件夹 '{args.input_folder}' 不存在!")
        return

    # 处理图片
    process_images(args.input_folder, args.output_folder, args.width)

if __name__ == "__main__":
    # 如果直接运行脚本而没有命令行参数，使用交互模式
    import sys
    if len(sys.argv) == 1:
        print("请选择运行模式：")
        print("1. 使用命令行参数运行")
        print("2. 使用交互模式运行")
        choice = input("请输入选项 (1或2): ").strip()

        if choice == "1":
            print("\n使用方式: python script.py <输入文件夹> <输出文件夹> [--width 宽度]")
            print("示例: python script.py ./images ./output --width 150")
            print("\n说明: 脚本会自动复制所有文件到输出目录，")
            print("      宽度超过阈值的图片会被缩放，其他文件直接复制")
        else:
            input_folder = input("请输入输入文件夹路径: ").strip()
            output_folder = input("请输入输出文件夹路径: ").strip()
            if input_folder and output_folder:
                width = input("请输入目标宽度 (直接回车使用150): ").strip()
                width = int(width) if width else 150

                process_images(input_folder, output_folder, width)
    else:
        main()

input("\n按Enter键退出...")
