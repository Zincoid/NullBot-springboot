import os
import re
import sys


def remove_prefix_in_directory(directory_path, prefix="立绘_"):
    """
    删除指定目录中所有文件名中的指定前缀

    Args:
        directory_path: 目录路径
        prefix: 要去除的前缀，默认为"立绘_"
    """
    # 检查目录是否存在
    if not os.path.exists(directory_path):
        print(f"错误：目录 '{directory_path}' 不存在！")
        return False

    if not os.path.isdir(directory_path):
        print(f"错误：'{directory_path}' 不是目录！")
        return False

    # 获取目录中的所有文件
    files = os.listdir(directory_path)

    # 计数器
    renamed_count = 0
    skipped_count = 0

    print(f"开始处理目录: {directory_path}")
    print(f"要去除的前缀: '{prefix}'")
    print("=" * 50)

    # 遍历所有文件
    for filename in files:
        # 构建完整的文件路径
        old_path = os.path.join(directory_path, filename)

        # 只处理文件，跳过目录
        if not os.path.isfile(old_path):
            continue

        # 检查文件名是否以指定前缀开头
        if filename.startswith(prefix):
            # 去掉前缀，生成新文件名
            new_filename = filename[len(prefix):]
            new_path = os.path.join(directory_path, new_filename)

            # 检查新文件名是否已存在
            if os.path.exists(new_path):
                print(f"警告：'{new_filename}' 已存在，跳过重命名 '{filename}'")
                skipped_count += 1
                continue

            # 重命名文件
            try:
                os.rename(old_path, new_path)
                print(f"✓ 重命名: {filename} -> {new_filename}")
                renamed_count += 1
            except Exception as e:
                print(f"✗ 重命名失败 '{filename}': {e}")
                skipped_count += 1
        else:
            # 文件名不以指定前缀开头，跳过
            skipped_count += 1

    print("=" * 50)
    print(f"处理完成！")
    print(f"成功重命名: {renamed_count} 个文件")
    print(f"跳过: {skipped_count} 个文件")

    return True


def main():
    """
    主函数：支持命令行参数和交互式输入
    """
    # 检查是否通过命令行参数提供了目录路径
    if len(sys.argv) > 1:
        directory_path = sys.argv[1]
        # 可选的第二个参数：自定义前缀
        prefix = sys.argv[2] if len(sys.argv) > 2 else "立绘_"
    else:
        # 交互式输入
        print("文件前缀批量重命名工具")
        print("=" * 50)
        directory_path = input("请输入要处理的目录路径: ").strip()
        prefix = input("请输入要去除的前缀 (默认: '立绘_'): ").strip()

        # 如果用户没有输入前缀，使用默认值
        if not prefix:
            prefix = "立绘_"

    # 去除路径两端的引号（如果用户拖拽文件夹到命令行可能会带有引号）
    directory_path = directory_path.strip('"\'')

    # 执行重命名操作
    remove_prefix_in_directory(directory_path, prefix)

    input("\n按Enter键退出...")

if __name__ == "__main__":
    main()