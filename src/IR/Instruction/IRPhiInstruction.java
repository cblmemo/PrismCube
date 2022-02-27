package IR.Instruction;

import FrontEnd.IRVisitor;
import IR.IRBasicBlock;
import IR.Operand.IROperand;
import IR.Operand.IRRegister;
import IR.TypeSystem.IRTypeSystem;
import MiddleEnd.Utils.CloneManager;

import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class IRPhiInstruction extends IRInstruction {
    private final IRRegister resultRegister;
    private final ArrayList<IROperand> values = new ArrayList<>();
    private final ArrayList<IRBasicBlock> blocks = new ArrayList<>();
    private final IRTypeSystem resultType;

    public IRPhiInstruction(IRBasicBlock parentBlock, IRRegister resultRegister, IRTypeSystem resultType) {
        super(parentBlock);
        this.resultRegister = resultRegister;
        this.resultType = resultType;
        resultRegister.setDef(this);
    }

    public void addCandidate(IROperand value, IRBasicBlock block) {
        values.add(value);
        blocks.add(block);
        value.addUser(this);
        block.getLabel().addUser(this);
    }

    public void removeCandidate(IRBasicBlock block) {
        int index = blocks.indexOf(block);
        assert index >= 0 : blocks + " " + block;
        blocks.get(index).getLabel().removeUser(this);
        removeUse(blocks.get(index).getLabel());
        blocks.remove(index);
        values.get(index).removeUser(this);
        removeUse(values.get(index));
        values.remove(index);
    }

    public void replaceSourceBlock(IRBasicBlock oldBlock, IRBasicBlock newBlock) {
        boolean replaced = false;
        for (int i = 0; i < blocks.size(); i++) {
            if (blocks.get(i) == oldBlock) {
                blocks.set(i, newBlock);
                oldBlock.getLabel().removeUser(this);
                removeUse(oldBlock.getLabel());
                newBlock.getLabel().addUser(this);
                replaced = true;
                break;
            }
        }
        assert replaced : this + " " + blocks + " " + oldBlock + " " + newBlock;
    }

    public void forEachCandidate(BiConsumer<IRBasicBlock, IROperand> consumer) {
        for (int i = 0; i < blocks.size(); i++) consumer.accept(blocks.get(i), values.get(i));
    }

    public ArrayList<IRBasicBlock> getBlocks() {
        return blocks;
    }

    public ArrayList<IROperand> getValues() {
        return values;
    }

    public IRRegister getResultRegister() {
        return resultRegister;
    }

    @Override
    public void replaceUse(IROperand oldOperand, IROperand newOperand) {
        super.replaceUse(oldOperand, newOperand);
        for (int i = 0; i < values.size(); i++) {
            if (values.get(i) == oldOperand) {
                oldOperand.removeUser(this);
                values.set(i, newOperand);
                newOperand.addUser(this);
            }
        }
    }

    @Override
    public void forEachNonLabelOperand(Consumer<IROperand> consumer) {
        consumer.accept(resultRegister);
        values.forEach(consumer);
    }

    @Override
    public IRInstruction cloneMySelf(CloneManager m) {
        IRPhiInstruction phi = new IRPhiInstruction(m.get(getParentBlock()), (IRRegister) m.get(resultRegister), resultType);
        forEachCandidate((block, value) -> phi.addCandidate(m.get(value), m.get(block)));
        return phi;
    }

    @Override
    public IRRegister getDef() {
        return resultRegister;
    }

    @Override
    public boolean noUsersAndSafeToRemove() {
        return false;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(resultRegister).append(" = phi ").append(resultType).append(" ");
        for (int i = 0; i < values.size(); i++) {
            if (i != 0) builder.append(", ");
            builder.append("[ ").append(values.get(i)).append(", ").append(blocks.get(i)).append(" ]");
        }
        return builder.toString();
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
