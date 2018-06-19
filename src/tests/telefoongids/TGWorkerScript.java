package tests.telefoongids;

import java.awt.Color;
import java.awt.Image;
import java.util.ArrayList;
import java.util.logging.Level;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import containers.Result;
import containers.concurrency.RandomGenerator;
import graphicalinterfaces.MessagePanel;
import models.TerminationLimit;
import workers.WorkerFactory;
import workers.WorkerTelefoonGids;

public class TGWorkerScript {

	private static String[] links = {
			"https://www.detelefoongids.nl/suze-s-soesjeshuis/18935240/5-1/",
			"https://www.detelefoongids.nl/buskens-brood-en-banketbakkerij/9024307/5-1/",
			"https://www.detelefoongids.nl/ghadir-ziani/20830669/5-1/",
			"https://www.detelefoongids.nl/durk-bakker-onderwijsadvies-beeldcoaching/20991106/5-1/",
			"https://www.detelefoongids.nl/raja-yoga-ruth-bakker/21144883/5-1/",
			"https://www.detelefoongids.nl/manimal-works/19693493/5-1/",
			"https://www.detelefoongids.nl/bakker-schilders-en-klusbedrijf-jacob/19970876/5-1/",
			"https://www.detelefoongids.nl/bakker-kinder-fysiotherapeut-l/20347414/5-1/",
			"https://www.detelefoongids.nl/bakker-business/45302617/5-1/",
			"https://www.detelefoongids.nl/timmer-onderhoudsbedrijf-l-c-bakker/20500055/5-1/",
			"https://www.detelefoongids.nl/bakker-ammerlaan/111468790080/5-1/",
			"https://www.detelefoongids.nl/almina-patisserie-bv/20848925/5-1/",
			"https://www.detelefoongids.nl/potters-en-van-beek-vof/20702488/5-1/",
			"https://www.detelefoongids.nl/donuts-bagels-bv/16838506/5-1/",
			"https://www.detelefoongids.nl/cabolicious/21142227/5-1/",
			"https://www.detelefoongids.nl/muskitaart/21054861/5-1/",
			"https://www.detelefoongids.nl/klootwijk-bakker/20275631/5-1/",
			"https://www.detelefoongids.nl/arkesteyn-brood-en-banketbakkerij-j-a-m/9030326/5-1/",
			"https://www.detelefoongids.nl/j-van-ballegoie/20732306/5-1/",
			"https://www.detelefoongids.nl/leen-bakker-bv/173710640010/5-1/",
			"https://www.detelefoongids.nl/bakker-glashandel/13153133/5-1/",
			"https://www.detelefoongids.nl/marakesh-bakkerij/16996774/5-1/",
			"https://www.detelefoongids.nl/zju-zo/20505740/5-1/",
			"https://www.detelefoongids.nl/lijnbaan-bakkerij-bart/14625628/5-1/",
			"https://www.detelefoongids.nl/meesterbakker-uljee/11254546/5-1/",
			"https://www.detelefoongids.nl/dijk-banketbakkerij-j-van/18988928/5-1/",
			"https://www.detelefoongids.nl/halk-vof-bakkerij/9029866/5-1/",
			"https://www.detelefoongids.nl/altuntas-bakkerij/14898633/5-1/",
			"https://www.detelefoongids.nl/bagel-bakery/9038096/5-1/",
			"https://www.detelefoongids.nl/bw-rotterdam-t-h-o-d-n-backwerk/21052787/5-1/",
			"https://www.detelefoongids.nl/hugo-bakker-musicus/19565746/5-1/",
			"https://www.detelefoongids.nl/durk-bakker-onderwijsadvies-beeldcoaching/20991109/5-1/",
			"https://www.detelefoongids.nl/laurelle-bakker/20554686/5-1/",
			"https://www.detelefoongids.nl/hicret-bakkerij/9033682/5-1/",
			"https://www.detelefoongids.nl/casteleijn-bakkerij/189301540100/5-1/",
			"https://www.detelefoongids.nl/ozturkiyem-bakkerij/14639416/5-1/",
			"https://www.detelefoongids.nl/bakker-consultancy/15992595/5-1/",
			"https://www.detelefoongids.nl/pitstop-bakkers/19761533/5-1/",
			"https://www.detelefoongids.nl/jan-bakker-mesthandel-bv/20575773/5-1/",
			"https://www.detelefoongids.nl/bakker-bezey-riethoff-scholts-kloeg-monuta-uitvaartzorg-en/14375897/5-1/",
			"https://www.detelefoongids.nl/aksu-bakkerij/9033293/5-1/",
			"https://www.detelefoongids.nl/meesterbakker-uljee-kantoor/13184490/5-1/",
			"https://www.detelefoongids.nl/bakker-william/18889194/5-1/",
			"https://www.detelefoongids.nl/arkesteyn-brood-en-banketbakkerij-j-a-m/9027693/5-1/",
			"https://www.detelefoongids.nl/ballegoie-brood-en-banketbakkerij-jan-van/11112755/5-1/",
			"https://www.detelefoongids.nl/narjiss-banketbakkerij-lunchroom/17998232/5-1/",
			"https://www.detelefoongids.nl/patisserie-sweets-cakes/20898422/5-1/",
			"https://www.detelefoongids.nl/osmanli-tulumbacisi/20115158/5-1/",
			"https://www.detelefoongids.nl/younes-bakkerij-patisserie/13659037/5-1/",
			"https://www.detelefoongids.nl/jamama-bakkerij/17885931/5-1/",
			"https://www.detelefoongids.nl/bakker-garage-s/11110660/5-1/",
			"https://www.detelefoongids.nl/vof-van-brenkelen/20950944/5-1/",
			"https://www.detelefoongids.nl/bakker-s-oliehandel/16364176/5-1/",
			"https://www.detelefoongids.nl/bakkerij-vd-grijn/20992776/5-1/",
			"https://www.detelefoongids.nl/meziane-bakery/21381347/5-1/",
			"https://www.detelefoongids.nl/bakker-mr-o-j-r/13031415/5-1/",
			"https://www.detelefoongids.nl/leen-bakker-bv/173710640050/5-1/",
			"https://www.detelefoongids.nl/taart-geheimen/21164020/5-1/",
			"https://www.detelefoongids.nl/nebaca-nel-bakker-cateringservice/20539229/5-1/",
			"https://www.detelefoongids.nl/edwin-bakker-communicatie-design/21380548/5-1/",
			"https://www.detelefoongids.nl/break-time/21255124/5-1/",
			"https://www.detelefoongids.nl/kersten-brood-en-banketbakkerij/9021912/5-1/",
			"https://www.detelefoongids.nl/buskens-brood-en-banketbakkerij/90398170051/5-1/",
			"https://www.detelefoongids.nl/vanmenno-bv/16301447/5-1/",
			"https://www.detelefoongids.nl/gul-bakkerij/13891954/5-1/",
			"https://www.detelefoongids.nl/swirl-cupcake-and-more/21016612/5-1/",
			"https://www.detelefoongids.nl/grijn-bakkerij-vd/181635970070/5-1/",
			"https://www.detelefoongids.nl/pasa-warme-bakker/20730403/5-1/",
			"https://www.detelefoongids.nl/groen-banketbakkerij-chocolaterie/19225881/5-1/",
			"https://www.detelefoongids.nl/jan-bakker-mesthandel-bv/20575774/5-1/",
			"https://www.detelefoongids.nl/bakkerij-oujda/20564320/5-1/",
			"https://www.detelefoongids.nl/leen-bakker/20279949/5-1/",
			"https://www.detelefoongids.nl/bakker-b-praktijk-voor-fysiotherapie-kralingen/20057802/5-1/",
			"https://www.detelefoongids.nl/bakkerij-lexmond/9033174/5-1/",
			"https://www.detelefoongids.nl/havenaar-bv-bakkerij-h/11144839/5-1/",
			"https://www.detelefoongids.nl/gonzalez-bakker-m-l/20190933/5-1/",
			"https://www.detelefoongids.nl/cifci-u/19455913/5-1/",
			"https://www.detelefoongids.nl/klootwijk-bakkerij/14637830/5-1/",
			"https://www.detelefoongids.nl/meesterbakker-uljee/11254545/5-1/",
			"https://www.detelefoongids.nl/de-bakker-meer/21163468/5-1/",
			"https://www.detelefoongids.nl/bakker-auto-s-ed/18266015/5-1/",
			"https://www.detelefoongids.nl/breggen-bakkerij-vd/209850200030/5-1/",
			"https://www.detelefoongids.nl/bakkerskoning-vuurplaat-bv/20137546/5-1/",
			"https://www.detelefoongids.nl/aad-klootwijk-bakkerij/20116499/5-1/",
			"https://www.detelefoongids.nl/bakker-die-ze-bruin-bakt/13894264/5-1/",
			"https://www.detelefoongids.nl/bakker-dakramen/14217594/5-1/",
			"https://www.detelefoongids.nl/nieuw-fes-bakkerij/13390798/5-1/",
			"https://www.detelefoongids.nl/charlois-bakkerij/20070495/5-1/",
			"https://www.detelefoongids.nl/bakker-brocantieq/21248027/5-1/",
			"https://www.detelefoongids.nl/erlijn-bakker/20285466/5-1/",
			"https://www.detelefoongids.nl/klootwijk-bakkerij/18607246/5-1/",
			"https://www.detelefoongids.nl/bakker-pedicure-joke/16336878/5-1/",
			"https://www.detelefoongids.nl/bakker-scheepvaart/20807365/5-1/",
			"https://www.detelefoongids.nl/bakkerij-van-den-berg/18990060/5-1/",
			"https://www.detelefoongids.nl/bakker-klootwijk-liersen-cv/19837230/5-1/",
			"https://www.detelefoongids.nl/fysiotherapie-manuele-therapie-b-bakker/20057801/5-1/",
			"https://www.detelefoongids.nl/oz-mevlana-bakkerij/14209268/5-1/",
			"https://www.detelefoongids.nl/meesterbakker-uljee/13643481/5-1/",
			"https://www.detelefoongids.nl/vof-scheepvaartbedrijf-bakker/20516291/5-1/",
			"https://www.detelefoongids.nl/klootwijk-bakkerij-aad/20079936/5-1/",
			"https://www.detelefoongids.nl/janneke-bakker/19810877/5-1/",
			"https://www.detelefoongids.nl/bakkerij-nass/20530984/5-1/",
			"https://www.detelefoongids.nl/beydagi/14190466/5-1/",
			"https://www.detelefoongids.nl/horan-broodbakkerij/9032206/5-1/",
			"https://www.detelefoongids.nl/patissier-bv-de/20295756/5-1/",
			"https://www.detelefoongids.nl/banketbakkerij-schiebroek/17881949/5-1/",
			"https://www.detelefoongids.nl/bakker-autorijschool/13018603/5-1/",
			"https://www.detelefoongids.nl/aad-klootwijk-bakkerij/20141503/5-1/",
			"https://www.detelefoongids.nl/breggen-bakkerij-vd/209850200020/5-1/",
			"https://www.detelefoongids.nl/bakkerij-sevinc/20850697/5-1/",
			"https://www.detelefoongids.nl/goreme-bakkerij-lunchroom/20099262/5-1/",
			"https://www.detelefoongids.nl/steens-echte-bakker-guus/14516945/5-1/",
			"https://www.detelefoongids.nl/meesterbakker-uljee/13184473/5-1/",
			"https://www.detelefoongids.nl/nieck-bakker/20882476/5-1/",
			"https://www.detelefoongids.nl/bakker-steffen-nvm-makelaars-taxateurs/18007139/5-1/",
			"https://www.detelefoongids.nl/bakker-organisatieadvies-en-verandermanagement/20311605/5-1/",
			"https://www.detelefoongids.nl/cigkoftem-rotterdam/20740827/5-1/",
			"https://www.detelefoongids.nl/gwenny-bakker/21274121/5-1/",
			"https://www.detelefoongids.nl/davey-bakker/20989406/5-1/",
			"https://www.detelefoongids.nl/bloemhof-bv/14631724/5-1/",
			"https://www.detelefoongids.nl/buskens-brood-en-banketbakkerij/90398170041/5-1/"
	};

	public static void main(String[] args) throws Exception {

		String site = "http://www.detelefoongids.nl/";

		ArrayList<String> proxies = new ArrayList<String>();
		for(int l=0; l<10; l++){
			proxies.add("http://fishproxy.com/"); // One
		}	
		for(int l=0; l<10; l++){
			proxies.add("http://xitenow.com/"); // One
		}
		for(int l=0; l<10; l++){
			proxies.add("http://webanonymizer.org/");
		}
		for(int k=0; k<10; k++){
			proxies.add("https://www.proxfree.com/");
		}

		for(int i=0; i<10; i++){
			proxies.add("https://www.filterbypass.me/"); // 1
		}
		for(int i=0; i<10; i++){
			proxies.add("http://www.unblockmyweb.com/"); // 1
		}

		RandomGenerator gen = new RandomGenerator(proxies.size());

		TerminationLimit termination = new TerminationLimit("Page Limit",10);

		java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF); 
		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");

		UIManager.put("control", new Color(255,255,255));
		UIManager.put("nimbusOrange", new Color(51,98,140));

		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (Exception e) {}

		ImageIcon imageIcon = new ImageIcon("./Logo.png"); // load the image to a imageIcon
		Image image = imageIcon.getImage(); // transform it 
		Image logo = image.getScaledInstance(1000, 1000,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  

		JFrame frame = new JFrame("Test");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setIconImage(logo);

		/**
		 * 
		 */
		MessagePanel panel = new MessagePanel();
		frame.add(panel);
		frame.pack();
		frame.setLocationRelativeTo(null);
		//		frame.setVisible(true);

		for(String link : links) {
			while(true) {
				WorkerTelefoonGids worker = (WorkerTelefoonGids) WorkerFactory.getWorker(link, site, proxies, gen, termination, panel);

				try {
					Result result = worker.call();

					System.out.println(result);
					break;
				} catch (Exception e) {
					System.out.println(link + "\t" + e);
					//				e.printStackTrace();
				}
			}
		}
	}
}
