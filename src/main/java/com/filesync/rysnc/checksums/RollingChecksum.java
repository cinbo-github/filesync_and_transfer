package com.filesync.rysnc.checksums;


import com.filesync.rysnc.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author jiuyuehe
 *
 */
public class RollingChecksum {
	private Logger logger = LoggerFactory.getLogger(RollingChecksum.class);
	/**
	 * 原始文件
	 */
	private FileChecksums srcFile;

	/**
	 * 修改后的文件
	 */
	private File updateFile;

	/**
	 * 存储结果字段
	 */
	private List<DiffCheckItem> diffList;

	/**
	 * 随机文件读取
	 */
	private RandomAccessFile raf;

	/**
	 * 随机文件读取
	 */
	private RandomAccessFile diffraf;

	private long current_weakChecksum=0;

	public RollingChecksum() {
	}

	public RollingChecksum(FileChecksums srcFile, File updateFile, List<DiffCheckItem> diffList) {
		this.srcFile = srcFile;
		this.diffList = diffList;
		this.updateFile = updateFile;
	}

	@Override
	public String toString() {
		return "RollingChecksum [srcFile=" + srcFile + "]";
	}

	/**
	 * 文件info 组装成map
	 * 
	 * @param
	 * @return
	 */
	private Map<Long, BlockChecksums> converte2Map() {

		List<BlockChecksums> blist = srcFile.getBlockChecksums();

		Map<Long, BlockChecksums> map = new HashMap<Long, BlockChecksums>();

		for (BlockChecksums blockChecksums : blist) {
			map.put(blockChecksums.getWeakChecksum(), blockChecksums);
		}
		return map;
	}

	/**
	 * 算法
	 */
	public void rolling() {
		Map<Long, BlockChecksums> srcMap = converte2Map();
		if (diffList == null) {
			diffList = new ArrayList<DiffCheckItem>();
		}

		long fileLength = updateFile.length();
		// 偏移量
		int offset = 0;
		do {
			offset = checkBlk(srcMap, offset, diffList);
		} while (offset < fileLength);

		if (diffraf != null) {
			try {
				diffraf.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (raf != null) {
			try {
				raf.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}



	}

	/**
	 * 滚动对比
	 * 
	 * @param srcMap
	 * @param
	 * @return
	 */
	private int checkBlk(Map<Long, BlockChecksums> srcMap, int offset, List<DiffCheckItem> difList) {
		int start = offset;
		BlockChecksums bck = null; // 新老文件相同的块，老文件的
		BlockChecksums blk = null; // 新文件取出的块
		BlockChecksums lastblk = null;
		for (; start < updateFile.length(); start++) {

			blk = getNextBlock(start,lastblk);

			lastblk = blk;
			if (srcMap.containsKey(blk.getWeakChecksum())) {
				bck = srcMap.get(blk.getWeakChecksum());
				blk.getStrongChecksum();
				if (bck.getHexStrongChecksum().equals(blk.getHexStrongChecksum())) {
					current_weakChecksum = 0;
					break;
				}
			}
		}

		if (blk != null) {
			int len = start - offset;

			if (len > 0) {
				try {
					if (diffraf == null) {
						diffraf = new RandomAccessFile(updateFile, "r");
					}
					byte[] by = new byte[len];
					diffraf.seek(offset);
					diffraf.read(by, 0, len);

					DiffCheckItem dl = new DiffCheckItem();
					dl.setMatch(false);
					dl.setData(by);
					difList.add(dl);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				if (bck != null) {
					DiffCheckItem dl = new DiffCheckItem();
					dl.setIndex(bck.getIndex());
					dl.setMatch(true);
					difList.add(dl);
				}
			} else {
				DiffCheckItem dl = new DiffCheckItem();
				dl.setIndex(bck.getIndex());
				dl.setMatch(true);
				difList.add(dl);
			}
			return start + Constants.BLOCK_SIZE;
		} else {
			return start;
		}
	}
	
	/**
	 * 根据文件偏移量获取每一块的checksum
	 * 
	 * @param offset
	 * @return BlockChecksums
	 */
	private BlockChecksums getNextBlock(int offset,BlockChecksums lastblk) {
		byte[] buf = new byte[Constants.BLOCK_SIZE];
		try {
			if (raf == null) {
				raf = new RandomAccessFile(updateFile, "r");
			}
			raf.seek(offset);
			int re = raf.read(buf, 0, Constants.BLOCK_SIZE);

			if(current_weakChecksum == 0) {
				BlockChecksums blk = new BlockChecksums(buf, offset, re);
				current_weakChecksum = blk.getWeakChecksum();
				return blk;
			}else{
				byte c1 = lastblk.getDataPtr()[0];
				byte c2 = buf[re-1];
				BlockChecksums blk = new BlockChecksums(buf, (int)current_weakChecksum,offset, re,c1,c2);
				current_weakChecksum = blk.getWeakChecksum();
				return blk;
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public FileChecksums getSrcFile() {
		return srcFile;
	}

	public void setSrcFile(FileChecksums srcFile) {
		this.srcFile = srcFile;
	}

	public File getUpdateFile() {
		return updateFile;
	}

	public void setUpdateFile(File updateFile) {
		this.updateFile = updateFile;
	}

}
