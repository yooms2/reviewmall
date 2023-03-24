package com.lec.rm.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.lec.rm.dao.BoardDao;
import com.lec.rm.dto.BoardDto;
import com.lec.rm.dto.MemberDto;
import com.oreilly.servlet.MultipartRequest;
import com.oreilly.servlet.multipart.DefaultFileRenamePolicy;

public class BoardReplyService implements Service {
	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response) {
		String path = request.getRealPath("fileUp");
		int maxSize = 1024*1024*2;
		String bfilename = "";
		try {
			MultipartRequest mRequest = new MultipartRequest(request, path, maxSize, "utf-8", new DefaultFileRenamePolicy());
			Enumeration<String> params = mRequest.getFileNames();
			String param = params.nextElement();
			bfilename = mRequest.getFilesystemName(param);
			HttpSession session = request.getSession();
			MemberDto member = (MemberDto) session.getAttribute("member");
			if(member == null) {
				request.setAttribute("boardResult", "로그인 후 작성이 가능합니다");
				return;
			}
			String mid = member.getMid();
			String mname = member.getMname();
			String mnickname = member.getMnickname();
			String btitle = mRequest.getParameter("btitle");
			String bcontent = mRequest.getParameter("bcontent");
			int bgroup = Integer.parseInt(mRequest.getParameter("bgroup"));
			int bstep = Integer.parseInt(mRequest.getParameter("bstep"));
			int bindent = Integer.parseInt(mRequest.getParameter("bindent"));
			BoardDao bDao = BoardDao.getInstance();
			BoardDto bDto = new BoardDto(0, mid, mname, mnickname, btitle, bcontent, bfilename, 0, bgroup, bstep, bindent, null);
			int result = bDao.boardReply(bDto);
			if(result == BoardDao.SUCCESS) {
				request.setAttribute("boardResult", "답변글 작성하였습니다");
			} else {
				request.setAttribute("boardResult", "답변글 작성에 실패하였습니다");
			}
			request.setAttribute("pageNum", mRequest.getParameter("pageNum"));
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		if(bfilename != null) {
			InputStream is = null;
			OutputStream os = null;
			File serverFile = new File(path + "/" + bfilename);
			try {
				is = new FileInputStream(serverFile);
				os = new FileOutputStream("C:/Project/reviewmall/WebContent/fileUp/" + bfilename);
				byte[] bs = new byte[(int)serverFile.length()];
				while(true) {
					int readByteCnt = is.read(bs);
					if(readByteCnt==-1) break;
					os.write(bs, 0, readByteCnt);
				}
			} catch (IOException e) {
				System.out.println(e.getMessage());
			} finally {
				try {
					if(os!=null) os.close();
					if(is!=null) is.close();
				} catch (IOException e) {
					System.out.println(e.getMessage());
				}
			}
		}
	}
}
