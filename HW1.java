package decisionTree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
/**
 * Decision Tree Algorithm
 * ID3(Examples,  Target_Abribute,  Abributes)  
•  Create a Root node for the tree.  
•  If all Examples are positive, Return the single-­‐node tree Root, with label = +.  
•  If all Examples are negative, Return the single-­‐node tree Root, with label = -.
•  If Abributes	is empty,Return the single-­‐node tree Root, with label = most common value of Target_abribute in Examples.	  
•  Otherwise	
  
	–  A  <-­‐ abribute from Abributes that best classifies examples 
	–  The decision abribute for Root <-­‐ A 
	–  For each	possible value, v i of A,	

		•  Add a new tree branch below Root, corresponding to the test A = v i .  
		•  Let Examples vi be the subset of Examples that have value v i  for A  
		•  If Examples vi is empty,
			–  Then add a new leaf node with label = most common value of Target_abribute in Examples.
		•  Else, below this new branch add the subtree 
			–  ID3(Examples vi , Target_abribute, Abributes – {A})  
•  End	
•  Return Root
 * @author milan
 *
 */
class Root{
	String name;
	int value;
	int branch;
	List<Root> childList;
	
	public Root(){
		branch = -1;
		value = -1;
		childList = new ArrayList<Root>();
	}
}
public class HW1 {

	public static void main(String[] args) {
		String training_set_file_path = "/home/milan/Downloads/MS/Fall17/CS580L/hw1/dataset 1/dataset 1/training_set.csv";
		MyFileReader reader = new MyFileReader(training_set_file_path);
		List<List<Integer>> matrix = reader.read();
		List<String> attributes = reader.attributes;
		int[] attributesCheck = new int [attributes.size()];
		HW1 hw1 = new HW1();
		Root root = hw1.createTree(matrix, attributes, attributesCheck);
		System.out.println(root);
		hw1.printTree(root,0);
		List<Integer> row;
		int correctClassificationCount = 0;
		for(int i=0; i<matrix.size();i++){
			row = matrix.get(i);
			hw1.checkTree(row, reader.attribMap, root, 0);
			if(row.get(row.size()-1) == row.get(row.size()-2))
				correctClassificationCount++;
		}
		System.out.println("Accuracy = "+(double)correctClassificationCount/matrix.size());
	}
	
	public void printTree(Root root,int count){
		List <Root>childList = root.childList;
		for(int i=0; i<childList.size(); i++){
			Root nodeI = childList.get(i);
			if(nodeI.childList.size()!=0){
				for(int j=0; j<count; j++)
					System.out.print("|   ");
				System.out.println(root.name+" = "+nodeI.branch+" :");
				count+=1;
				printTree(nodeI, count);
				count-=1;
			}else{
				for(int j=0; j<count; j++)
					System.out.print("|   ");
				System.out.println(root.name+"= "+nodeI.branch +" : "+nodeI.value);	
			}
		}
	}
	
	public void checkTree(List<Integer> row, Map<String, Integer> attribMap, Root root,int count){
		List <Root>childList = root.childList;
		for(int i=0; i<childList.size(); i++){
			Root nodeI = childList.get(i);
			if(nodeI.childList.size()!=0){
				int rootIndex = attribMap.get(root.name);
				int rootVal = row.get(rootIndex);
				if(rootVal != nodeI.branch)
					continue;
				count+=1;
				checkTree(row, attribMap, nodeI, count);
				count-=1;
			}else{
				int rootIndex = attribMap.get(root.name);
				int rootVal = row.get(rootIndex);
				if(rootVal == nodeI.branch){
					row.set(row.size()-1, nodeI.value);
					return;
				}	
			}	
			
		}		
	}
	
	
	public Root createTree(List<List<Integer>> matrix, List<String> attributes, int[] attributesCheck){
		//Create a Root node for the tree
		Root root = new Root();
		int rows = matrix.size();
		if(rows == 0)
			return null;
		int count0 = 0, count1=0, ansIndex = matrix.get(0).size() -1;;
		for(int i=0; i<rows; i++){
			int no = (int)((List)matrix.get(i)).get(ansIndex-1);
			if(no==0)
				count0++;
			else if(no ==1)
				count1++;
		}
		//If all Examples are positive, Return the single-node tree Root, with label = +
		//If all Examples are negative, Return the single-node tree Root, with label = -
		if(rows == count0 || rows == count1){
			if(rows == count0)
				root.value = 0;
			else if(rows == count1)
				root.value = 1;
			return root;
		}
		double entropy = calculateEntropy(count0, count1);
		//System.out.println(entropy);
		int attributeIndex = -1; double infoGain =-1;
		for(int i=0; i<attributes.size(); i++){
			if(attributesCheck[i] == 1)
				continue;
			double infoGainVal = calculateInformationGain(matrix, i);
			double temp = entropy - infoGainVal;  
			if((temp) > infoGain){
				infoGain = temp;
				attributeIndex = i;
			}
		}
		//A <-- the attribute from Attributes that best* classifies Examples	
		//System.out.println(attributeIndex);
		//The decision attribute for Root <-- A
		root.name = attributes.get(attributeIndex);
		int [] attribVal = {0,1,2};
		attributesCheck[attributeIndex] = 1;
		//For each possible value, vi, of A,
		for(int j=0; j<attribVal.length; j++){
			List<List<Integer>> newMatrix = new ArrayList<List<Integer>>();
			//Let Examples,, be the subset of Examples that have value vi for A
			for(int i=0; i<rows; i++){
				int no = (int)((List)matrix.get(i)).get(attributeIndex);
				if(no == j)
					newMatrix.add(matrix.get(i));
			}	
			Root child = createTree(newMatrix,attributes, attributesCheck);
			if(null != child){
				//System.out.println(child.name+" "+child.value);
				child.branch = j;
				//Add a new tree branch below Root, corresponding to the test A = vi				
				root.childList.add(child);
			}
		}
		attributesCheck[attributeIndex] = 0;
		return root;
	}
	
	public double calculateEntropy(int count0, int count1){
		int size = count0 + count1;
		if(size == 0)
			return -1;
		//System.out.println("count0 = "+ count0 + " count1 = "+count1);
		double probability0 = (double)count0/size;
		double probability1 = (double)count1/size;
		double entropy = 0;
		if(probability0 !=0)
			entropy += ((probability0)*(-1)*Math.log(probability0)/Math.log(2));
		if(probability1 !=0)
			entropy += ((probability1)*(-1)*Math.log(probability1)/Math.log(2));
		return entropy;
	}

	public double calculateInformationGain (List<List<Integer>> matrix, int attribIndex){
		int ansIndex = matrix.get(0).size() -2;
		int attribVal0Count =0, attribVal1Count=0, attribVal2Count=0;
		int attribVal0Count0 =0, attribVal1Count0=0, attribVal2Count0=0;
		int attribVal0Count1 =0, attribVal1Count1=0, attribVal2Count1=0;
		int attribVal, result, givenVal;
		for(int i=0; i<matrix.size();i++){
			attribVal = matrix.get(i).get(attribIndex);
			result = matrix.get(i).get(ansIndex);
			if(attribVal ==0 && result ==0 ){
				attribVal0Count0++;
			}else if(attribVal ==0 && result ==1  ){
				attribVal0Count1++;
			}else if(attribVal ==1 && result ==0  ){
				attribVal1Count0++;
			}else if(attribVal ==1 && result ==1  ){
				attribVal1Count1++;
			}else if(attribVal ==2 && result ==0  ){
				attribVal2Count0++;
			}else if(attribVal ==2 && result ==1  ){
				attribVal2Count1++;
			}		
		}
		attribVal0Count = attribVal0Count0 + attribVal0Count1; 
		attribVal1Count = attribVal1Count0 + attribVal1Count1; 
		attribVal2Count = attribVal2Count0 + attribVal2Count1;
		int size = attribVal0Count + attribVal1Count + attribVal2Count;
		
		double attrib0Prob0=0, attrib0Prob1=0, attrib1Prob0=0, attrib1Prob1=0, 
				attrib2Prob0=0, attrib2Prob1=0;
		double infoGain = 0, temp0=0, temp1=0, temp2=0;
		
		if(attribVal0Count!=0){
			attrib0Prob1 =  (double)attribVal0Count1/attribVal0Count;
			attrib0Prob0 =  (double)attribVal0Count0/attribVal0Count;			
			temp0 += attrib0Prob1==0?0 : (-1 * attrib0Prob1 * Math.log(attrib0Prob1)/ Math.log(2));
			temp0 += attrib0Prob0==0?0 : (-1 * attrib0Prob0 * Math.log(attrib0Prob0)/ Math.log(2));
			infoGain =  ((double)attribVal0Count/size)*temp0;
			
		}	
		if(attribVal1Count!=0){
			attrib1Prob1 =  (double)attribVal1Count1/attribVal1Count;
			attrib1Prob0 =  (double)attribVal1Count0/attribVal1Count;
			temp1 += attrib1Prob1==0? 0 : (-1 * attrib1Prob1 * Math.log(attrib1Prob1)/ Math.log(2));
			temp1 += attrib1Prob0==0? 0 : (-1 * attrib1Prob0 * Math.log(attrib1Prob0)/ Math.log(2));
			infoGain =  infoGain + ((double)attribVal1Count/size)*temp1;
		}
			
		if(attribVal2Count!=0){
			attrib2Prob1 =  (double)attribVal2Count1/attribVal2Count;
			attrib2Prob0 =  (double)attribVal2Count0/attribVal2Count;
			temp2 += attrib2Prob1==0?0 : (-1 * attrib2Prob1 * Math.log(attrib2Prob1)/ Math.log(2));
			temp2 += attrib2Prob0==0?0 : (-1 * attrib2Prob0 * Math.log(attrib2Prob0)/ Math.log(2));
			infoGain = infoGain + ((double)attribVal2Count/size)*temp2;
		}	
		//System.out.println(infoGain);
		return infoGain;
	}

}